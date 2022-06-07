//
// Created by 飞拍科技 on 2019/3/2.
//

#include "HardDecoder.h"
#include <sys/time.h>


HardDecoder::HardDecoder(VideoType type):fType(type),fCodec(NULL),fFormat(NULL),fDecodeInited(false),fHardwareSupport(true),fWindow(NULL)
{
    LOGD("HardDecoder()");
    memset(pps_buffer, 0, PPS_SPS_MAX_SIZE);
    memset(sps_buffer, 0, PPS_SPS_MAX_SIZE);
    memset(vps_buffer,0,PPS_SPS_MAX_SIZE);
    pps_size = 0;
    sps_size = 0;
    vps_size = 0;

    nalu_buffer = (uint8_t*)malloc(NALU_MAX_SIZE);
    memset(nalu_buffer,0,NALU_MAX_SIZE);
    aud_buffer = (uint8_t*)malloc(AUD_MAX_SIZE);
    memset(aud_buffer,0,AUD_MAX_SIZE);
    aud_size = 0;
    aud_nal_count = 0;

    _sps_w = 0;
    _sps_h = 0;
    _sps_fps = 0;
    _pic_slice_count = 0;

    pthread_mutex_init(&fMutex, NULL);
}

HardDecoder::~HardDecoder()
{
    LOGD("~HardDecoder()");
    if (nalu_buffer) {
        free(nalu_buffer);
    }
    if (aud_buffer) {
        free(aud_buffer);
    }
    destroyDecoderContext();
    pthread_mutex_destroy(&fMutex);
}

void HardDecoder::setDecoderType(VideoType type)
{
    pthread_mutex_lock(&fMutex);
    if (fType != type) {
        pthread_mutex_unlock(&fMutex);
        return;
    }
    // 先删除以前的解码器
    destroyDecoderContext();

    pthread_mutex_unlock(&fMutex);
}

void HardDecoder::setRenderWindow(ANativeWindow *window)
{
    fWindow = window;
}

void HardDecoder::resetDecoder()
{
    pps_size =0;
    sps_size = 0;
    vps_size = 0;

    aud_size = 0;
    aud_nal_count = 0;

    _sps_w = 0;
    _sps_h = 0;
    _sps_fps = 0;
    _pic_slice_count = 0;

    fDecodeInited = false;
    fPushedIFrame = false;
    fDecodeErrorNum = 0;

    destroyDecoderContext();
}

void HardDecoder::setNeedsReset()
{
    //    sps_size = 0;
//    pps_size = 0;
//    vps_size = 0;
    aud_size = 0;
    aud_nal_count = 0;
    _pic_slice_count = 0;
//    self.decoderInited = NO;
    fDecodeErrorNum = 0;
}

void HardDecoder::setWidthAndHeight(int width,int height)
{
    if (fFormat == NULL) {
        return;
    }
    AMediaFormat_setInt32(fFormat, AMEDIAFORMAT_KEY_WIDTH, width);
    AMediaFormat_setInt32(fFormat, AMEDIAFORMAT_KEY_WIDTH, height);
}

bool HardDecoder::doInitDecoder()
{
    if (fWindow == NULL) {
        LOGD("fWindow not init,return ");
        return false;
    }

    if (fType == VideoTypeH264) {
        fCodec = AMediaCodec_createDecoderByType("video/avc");
    } else if (fType == VideoTypeH265) {
        fCodec = AMediaCodec_createDecoderByType("video/hevc");
    } else {
        LOGD("error no this type %d", fType);
        return false;
    }

    if (fCodec == NULL) {   // unsuport hard decode
        LOGD("create AMediaCodec fail");
        setNeedsReset();
        fHardwareSupport = false;
        return false;
    }

    fFormat = AMediaFormat_new();
    if (fFormat == NULL) {
        LOGD("create AMediaFormat fail");
        setNeedsReset();
        fHardwareSupport = false;
        return false;
    }

    if (fType == VideoTypeH264) {
        if (pps_size <= 0 || sps_size <= 0) {
            return false;
        }
    }
    if (fType == VideoTypeH265) {
        if (pps_size <= 0 || sps_size <= 0 || vps_size <= 0) {
            return false;
        }
    }

    if (fType == VideoTypeH264) {
        // 此句赋值不能少，否则AMediaCodec_configure会失败
        AMediaFormat_setString(fFormat, AMEDIAFORMAT_KEY_MIME, "video/avc");
        AMediaFormat_setInt32(fFormat,AMEDIAFORMAT_KEY_COLOR_FORMAT,0x7F000789);
        AMediaFormat_setBuffer(fFormat, "csd-0", sps_buffer, sps_size);
        AMediaFormat_setBuffer(fFormat, "csd-1", pps_buffer, pps_size);
        AMediaFormat_setInt32(fFormat, AMEDIAFORMAT_KEY_WIDTH, 960); // 视频宽度
        AMediaFormat_setInt32(fFormat, AMEDIAFORMAT_KEY_HEIGHT, 720); // ﻿视频高度
    }
    if (fType == VideoTypeH265) {
        int extraDataLen = pps_size + sps_size + vps_size;
        uint8_t *extraData = new uint8_t[extraDataLen];
        memcpy(extraData, vps_buffer, vps_size);
        extraData += vps_size;
        memcpy(extraData, sps_buffer, sps_size);
        extraData += sps_size;
        memcpy(extraData, pps_buffer, pps_size);
        AMediaFormat_setBuffer(fFormat, "csd-0", extraData, extraDataLen);
        delete extraData;
    }
    const char *s = AMediaFormat_toString(fFormat);
    LOGD("format: %s window %p", s,fWindow);

    media_status_t st = AMediaCodec_configure(fCodec, fFormat, fWindow, NULL, 0);
    if (st != AMEDIA_OK) {
        LOGD("AMediaCodec_configure error %d",st);
        setNeedsReset();
        fHardwareSupport = false;
        return false;
    }

    st = AMediaCodec_start(fCodec);
    if (st != AMEDIA_OK) {
        LOGD("AMediaCodec_start error %d",st);
        setNeedsReset();
        fHardwareSupport = false;
        return false;
    }

    fDecodeInited = true;
    LOGD("doInitDecoder() sucess");
    return true;
}

VideoFrameYUV* HardDecoder::decode(uint8_t* packet,int packetSize)
{
    if (packet == NULL || packetSize <= 0 || !fHardwareSupport) {
        return NULL;
    }

    int remain_size = packetSize;
    uint8_t* remain_buffer = packet;

    clearSliceAnaContex();
    LOGD("packetSize %d",packetSize);

    while (remain_size > 0) {

        if (!fHardwareSupport) {
            return NULL;
        }

        // 首个nal的offset，即第一个开始码的位置
        int nal_start_offset = findNextNALStartCodeEndPos(remain_buffer, remain_size);
        if (nal_start_offset <= 0) {
            LOGD("未找到开始码0001或者001");
            break;
        }

        int second_start_code_offset = findNextNALStartCodePos(remain_buffer+nal_start_offset, remain_size-nal_start_offset);
        if (second_start_code_offset < 0) { //说明剩余的buffer都是属于这个nal包
            second_start_code_offset = remain_size - nal_start_offset;
        }

        int nal_playload_size = second_start_code_offset;  // 一个nal包的大小
        if (nal_playload_size > NALU_MAX_SIZE || nal_playload_size <= 0) {
            LOGD("错误的rbsp size==>%d",nal_playload_size);
            return NULL;
        }

        uint8_t *nalu_ptr = NULL;
        if (remain_buffer + nal_start_offset - packet >= 4) {   // 说明nalu是0001开头的，否则是001开头的，这里也需要将4个字节的0001加入
            nalu_ptr = remain_buffer + nal_start_offset - 4;
        } else {
            memcpy(nalu_buffer+4,packet+nal_start_offset,nal_playload_size);
            memcpy(nalu_buffer, naluStart4Byte1, 4);
            nalu_ptr = nalu_buffer;
        }

        if (fDecodeInited) {
            //分析slice header，一个slice包含至少一个宏块，至多包含一帧完整的图像压缩数据或者部分图像数据
            if (!nalAnalysis(nalu_ptr,nal_playload_size+4)) {

                if (fPushedIFrame) {
                    setNeedsReset();
                }
                LOGD("nal 不正确 %d",fPushedIFrame);
                return NULL;
            }
        } else {
            decodeInitWithSingleNAL(nalu_ptr,nal_playload_size+4);
        }

        remain_size = remain_size - nal_start_offset - second_start_code_offset;
        remain_buffer = remain_buffer + nal_start_offset + second_start_code_offset;
    }

    // 真正开始解码
    bool decode_success = false;
    if (canDecode()) {
//        if (!fPushedIFrame) {
//            if (!checkCurrentFrame()) {
//                LOGD("frame 出错！");
//                return NULL;
//            }
//
//            if (!loadIdrFrame(aud_buffer,aud_size)) {
//                LOGD("idr 解码失败 ");
//                setNeedsReset();
//                return NULL;
//            }
//
//            // 解析idr frame成功，结束本次解码
//            fPushedIFrame = true;
//            return NULL;
//        }
//        if (fPushedIFrame) {
//            if (!loadIdrFrame(aud_buffer,aud_size)) {    // h265 才有的
//                setNeedsReset();
//                return NULL;
//            }
//
//            fPushedIFrame = true;
//            return NULL;
//        }

//        if (checkCurrentFrame()) {
//
//            decode_success = decodeWithBuffer(aud_buffer,aud_size);
//        } else {
//            LOGD("frame 出错！");
//            decode_success = true;
//        }

        decode_success = decodeWithBuffer(aud_buffer,aud_size);
    } else {
        LOGD("没有接收到 frame");
        decode_success = true;
    }

    aud_size = 0;
    aud_nal_count = 0;
    return NULL;
}

void HardDecoder::destroyDecoderContext()
{
    if (fCodec != NULL) {
        AMediaCodec_stop(fCodec);
        AMediaCodec_delete(fCodec);
        fCodec = NULL;
    }

    if (fFormat != NULL) {
        AMediaFormat_delete(fFormat);
        fFormat = NULL;
    }
}

void HardDecoder::clearSliceAnaContex()
{

}

bool HardDecoder::decodeInitWithSingleNAL(uint8_t *data, int size)
{
    if (fType == VideoTypeH264) {

        h264InitWithSingleNAL(data,size);

        if (pps_size && sps_size) {
            destroyDecoderContext();
            int ret = h264_decode_seq_parameter_set_out(sps_buffer, sps_size, &_sps_w, &_sps_h, &_sps_fps, &_currentSPS);
            if (-1 == ret) {
                setNeedsReset();
                return false;
            }
//            setWidthAndHeight(_sps_w,_sps_h);
            return doInitDecoder();
        }

    } else {
        h265InitWithSingleNAL(data,size);

        if (pps_size && sps_size && vps_size) {
            destroyDecoderContext();

//            setWidthAndHeight(_sps_w,_sps_h);
            return doInitDecoder();
        }
    }

    return true;
}

bool HardDecoder::h264InitWithSingleNAL(uint8_t *data, int size)
{

    if (!pps_size || !sps_size) {
        if (!data || size <= 4 || size > PPS_SPS_MAX_SIZE) {
            return false;
        }

        /** 每一个NAL由一个字节的header和rgbsp组成，其中header构成如下:
         *  forbidden_zero_bit(1bit) + nal_ref_idc(2bit) + nal_unit_type(5bit)
         *  forbidden_zero_bit: 禁止位，初始为0，当网络发现NAL单元有比特错误时可设置该比特为1，以便接收方纠错或丢弃该单元
         *  nal_ref_idc nal: 重要性指标，值越大，越重要。解码器在解码处理不过来的时候，可以丢掉重要性为0的NAL
         *  nal_unit_type:nal类型，取值如下：
         */

        uint8_t nal_unit_header = data[4];
        uint8_t nal_header_forbidden_bit = nal_unit_header&0x80;    //取一个字节的高8位
        LOGD("h264 nal_header_forbidden_bit %d",nal_header_forbidden_bit);
        if (nal_header_forbidden_bit) {
            return false;
        }
        uint8_t nal_header_type = nal_unit_header&0x1f; // 取一个字节的低五位比特值
        LOGD("h264 nal_unit_header %d",nal_header_type);
        switch (nal_header_type) {
            case H264_NAL_SPS:    //sps 包含0001
                memcpy(sps_buffer,data,size);
                sps_size = size;
                break;
            case H264_NAL_PPS: //pps 包含0001
                memcpy(pps_buffer, data, size);
                pps_size = size;
                break;
            default:
                break;
        }
    }

    if (sps_size && pps_size) {
        return true;
    }
    return false;
}

bool HardDecoder::h265InitWithSingleNAL(uint8_t *data, int size)
{
    if (!pps_size || !sps_size || !vps_size) {
        if (!data || size <= 4 || size > PPS_SPS_MAX_SIZE) {
            return false;
        }

        /** 每一个NAL由两个个字节的header和rgbsp组成，其中header构成如下:
         |0|1|2|3|4|5|6|7|0|1|2|3|4|5|6|7|
         |F|  NAL Type |  LayerId  | Tid |
         *  forbidden_zero_bit(1bit) + nal_ref_idc(2bit) + nal_unit_type(5bit)
         *  forbidden_zero_bit: 禁止位，初始为0，当网络发现NAL单元有比特错误时可设置该比特为1，以便接收方纠错或丢弃该单元
         *  nal_unit_type:nal类型，取值如下：
         */

        uint8_t nal_unit_header = data[4];
        uint8_t nal_header_forbidden_bit = nal_unit_header&0x80;    //取一个字节的高8位
        if (nal_header_forbidden_bit) {
            return false;
        }
        uint8_t nal_header_type = (nal_unit_header & 0x7E)>>1; // 取一个字节的1到7位
        LOGD("h265 nal_unit_header %d",nal_header_type);
        switch (nal_header_type) {
            case H265_NAL_SPS:    //sps
                memcpy(sps_buffer,data+4,size-4);
                sps_size = size - 4;
                break;
            case H265_NAL_PPS: //pps
                memcpy(pps_buffer, data+4, size-4);
                pps_size = size-4;
                break;
            case H265_NAL_VPS: //vps
                memcpy(vps_buffer, data+4, size-4);
                vps_size = size-4;
                break;
            default:
                break;
        }
    }

    if (pps_size && sps_size&&vps_size) {
        return true;
    }
    return false;
}

bool HardDecoder::nalAnalysis(uint8_t *data, int size) {
    if (fType == VideoTypeH264) {
        return h264NalAnalysis(data,size);
    } else {
        return h265NalAnalysis(data,size);
    }
}

bool HardDecoder::h264NalAnalysis(uint8_t *data, int size)
{
    if (!data || size <=4) {
        return false;
    }
    uint8_t nal_unit_header = data[4];
    uint8_t nal_unit_header_forbidden_bit = nal_unit_header & 0x80;
    if (nal_unit_header_forbidden_bit) {    // 说明包要丢弃
        return false;
    }

    uint8_t nal_unit_type = nal_unit_header & 0x1f;
    switch (nal_unit_type) {    // 只有idr，和片段才加入在一起
        case H264_NAL_IDR:
        case H264_NAL_SLICE:
        case H264_NAL_DPA:
        case H264_NAL_DPB:
        case H264_NAL_DPC:
            break;
        default:
            return true;
    }

    size_t nal_size = size - 4;
    uint8_t *nal_size_ptr = (uint8_t*)&nal_size;
    if (aud_size + size < AUD_MAX_SIZE) {
        // 转换成大端序 对于ios 原先0001的四个字节换成该帧的大小，采用大端序;对于安卓，不需要这样替换
        uint8_t *nal_start_code = aud_buffer + aud_size;
//        nal_start_code[0] = nal_size_ptr[3];
//        nal_start_code[1] = nal_size_ptr[2];
//        nal_start_code[2] = nal_size_ptr[1];
//        nal_start_code[3] = nal_size_ptr[0];
        nal_start_code[0] = 0;
        nal_start_code[1] = 0;
        nal_start_code[2] = 0;
        nal_start_code[3] = 1;
        memcpy(aud_buffer+aud_size+4,data+4,size-4);
        aud_size += size;
        aud_nal_count++;

        // 分析slice header并添加到一起
        sliceProcess(data+5,size-5); // slice是从第五个字节开始
    } else {
        LOGD("aud_buffer not enough error %d",aud_size);
    }

    return true;
}

bool HardDecoder::h265NalAnalysis(uint8_t *data, int size)
{
    if (!data || size <=4) {
        return false;
    }
    uint8_t nal_unit_header = data[4];
    uint8_t nal_unit_header_forbidden_bit = nal_unit_header & 0x80;
    if (nal_unit_header_forbidden_bit) {    // 说明包要丢弃
        return false;
    }

    int nal_unit_type = (nal_unit_header & 0x7E) >> 1;
    LOGD("nal_unit_type1 %d size %d",nal_unit_type,size);
    if (nal_unit_type > H265_NAL_AUD) {
        return false;
    }

    size_t nal_size = size - 4;
    uint8_t *nal_size_ptr = (uint8_t*)&nal_size;
    if (aud_size + size < AUD_MAX_SIZE) {
        // 转换成大端序
        uint8_t *nal_start_code = aud_buffer + aud_size;
        nal_start_code[0] = nal_size_ptr[3];
        nal_start_code[1] = nal_size_ptr[2];
        nal_start_code[2] = nal_size_ptr[1];
        nal_start_code[3] = nal_size_ptr[0];

        memcpy(aud_buffer+aud_size+4,data+4,size-4);
        aud_size += size;
        aud_nal_count++;

        // 分析slice header并添加到一起
//        [self sliceProcess:data+5 size:size-5]; // slice是从第五个字节开始

    } else {
        LOGD("aud_buffer not enough error %d",aud_size);
    }

    return true;
}

void HardDecoder::sliceProcess(uint8_t *data, int size)
{
    H264SliceHeaderSimpleInfo info;
    info.frame_num = -1;
    info.slice_type = -1;
    info.first_mb_in_slice = -1;
    if(0 == h264_decode_slice_header(data, size, &_currentSPS, &info)){
        //sliceheader analyse succeed
        if (info.slice_type == AV_PICTURE_TYPE_P
            || info.slice_type == AV_PICTURE_TYPE_B
            || info.slice_type == AV_PICTURE_TYPE_I) {
            //processing only i frame and p frame
            if (_pic_slice_count < FRAME_MAX_SLICE_COUNT) {
                _pic_slices[_pic_slice_count] = info;
                _pic_slice_count++;
            }
        }
    }
    else{
        //Also placed in the wrong slice, will check it out later
        if (_pic_slice_count < FRAME_MAX_SLICE_COUNT) {
            _pic_slices[_pic_slice_count] = info;
            _pic_slice_count++;
        }
    }
}

bool HardDecoder::loadIdrFrame(uint8_t *data, int size)
{
    if (!data || size <=4 || size > AUD_MAX_SIZE) {
        return false;
    }

    uint8_t nal_unit_header= data[4];
    int nal_unit_type = nal_unit_header & 0x1f;
    int idr_value = H264_NAL_IDR;
    if (fType == VideoTypeH265) {
        nal_unit_type = (nal_unit_header & 0x7e) >> 1;
        idr_value = H265_NAL_IDR_W_RADL;
    }
    if (nal_unit_type != idr_value) {
        LOGD("nal_unit_type not equal idr_value nal_unit_type = %d",nal_unit_type);
        return false;
    }

    return decodeWithBuffer(data,size) != NULL;
};

void byte2hex(uint8_t *buffer,int size,char* dest) {
    char tmp[3];
    for (int i = 0; i < size; i++) {
        sprintf(tmp,"%02X",buffer[i]);
        memcpy(&dest[i*2],tmp,2);
    }
}

uint8_t* HardDecoder::decodeWithBuffer(uint8_t *compressedData, int length)
{
    if (length < 4 || !fDecodeInited || fCodec == NULL) {
        return NULL;
    }

    char ff[100];
    byte2hex(compressedData,40,ff);
    LOGD("这里==>%s",ff);

    LOGD("decodeWithBuffer size=%d",length);
    // 获取一个解码缓冲区的索引
    ssize_t inputbufIdx = AMediaCodec_dequeueInputBuffer(fCodec, 2000);
    LOGD("inputbufIdx %d",inputbufIdx);

    if (inputbufIdx >= 0) {
        size_t bufSize;
        // 根据索引获取解码缓冲区中的一个缓冲区
        uint8_t *buf = AMediaCodec_getInputBuffer(fCodec, inputbufIdx, &bufSize);
        if (buf != NULL && length <= bufSize) {
            struct timeval tm;
            gettimeofday(&tm,NULL);
            uint64_t time = tm.tv_sec*1000000L+tm.tv_usec;

            // 往缓冲区中填入未解码数据
            memcpy(buf, compressedData, length);
            media_status_t status = AMediaCodec_queueInputBuffer(fCodec, inputbufIdx, 0, bufSize, time /* pts */, 0);
            LOGD("解码结果 %d",status);
        }
    }

    AMediaCodecBufferInfo info;
    ssize_t outputBufInx = AMediaCodec_dequeueOutputBuffer(fCodec, &info, 0);
    LOGD("outputBufInx %d",outputBufInx);
//    while (outputBufInx >= 0) {
//        AMediaCodec_releaseOutputBuffer(fCodec, outputBufInx, info.size != 0);
//        outputBufInx = AMediaCodec_dequeueOutputBuffer(fCodec, &info, 0);
//        LOGD("outputBufInx123 %d",outputBufInx);
//    }
    if (outputBufInx >= 0) {
//        size_t outputBufSize;
//        uint8_t *outputBuf = AMediaCodec_getOutputBuffer(fCodec, outputBufInx, &outputBufSize);
//        auto format = AMediaCodec_getOutputFormat(fCodec);
//        LOGD("format changed to:1111111 %s", AMediaFormat_toString(format));
//            LOGD("这里");
        AMediaCodec_releaseOutputBuffer(fCodec, outputBufInx, info.size != 0);
        return NULL;
//        if (outputBuf != NULL) {
//            //pts = info.presentationTimeUs;
//            //int32_t pts32 = (int32_t) pts;
//            uint8_t *dst = (uint8_t *)outBuf;
//            memcpy(dst, outputBuf, MIN(mOriFrameSize, mFrameSize));
//
//            uint8_t *uvBuf = outputBuf + mFrameSize;
//
//            uint32_t uvSize = MIN(mOriFrameSize >> 1, mFrameSize >> 1);
//            uint8_t *uBuf = dst  + mOriFrameSize;
//            uint8_t *vBuf = uBuf + (mOriFrameSize >> 2);
//            memcpy(uBuf, uvBuf, uvSize);
//            AMediaCodec_releaseOutputBuffer(mMediaCodec, outbufidx, info.size != 0);
//            return 1;
//        }

        if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
            LOGD("output EOS");
        }
        AMediaCodec_releaseOutputBuffer(fCodec, outputBufInx, info.size != 0);
    } else if (outputBufInx == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
        LOGD("output buffers changed");
    } else if (outputBufInx == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
        auto format = AMediaCodec_getOutputFormat(fCodec);
        LOGD("format changed to: %s", AMediaFormat_toString(format));
        AMediaFormat_delete(format);
        AMediaCodec_flush(fCodec);
    } else if (outputBufInx == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        LOGD("no output buffer right now");
    } else {
        LOGD("unexpected info code: %zd", outputBufInx);
    }

    return NULL;
}

bool HardDecoder::canDecode()
{
    return aud_size > 0 && aud_nal_count > 0;
}

bool HardDecoder::checkCurrentFrame()
{
    if (fType == VideoTypeH264) {
        return h264CheckCurentFrame();
    } else {
        return h265CheckCurentFrame();
    }
}

bool HardDecoder::h264CheckCurentFrame()
{
    if (_pic_slice_count <= 0 || _pic_slice_count >= FRAME_MAX_SLICE_COUNT) {
        LOGD("出错 %d",_pic_slice_count);
        return false;
    }
    if (!_sps_w || !_sps_h) {
        LOGD("出错 _sps_w %d _sps_h %d",_sps_w,_sps_h);
        return false;
    }
    /** 一个片组有至少一个片组成，如果不采用DP(数据分割机制)，一个片就是一个NALU，一个NALU也是一个片，否则一个片由多个NALU组成
     *  AUD 是图像分隔符，AUD类型的NALU只有6个字节，AUD之间为一张完整的图像
     *  一个片组内的frame_num必须是相同的
     *  片组内的宏块索引按照顺序从0开始
     */
    int frame_index = _pic_slices[0].frame_num;
    int last_slice_first_mb = 0;
    int firt_slice_first_mb_in_slice = _pic_slices[0].first_mb_in_slice;
    if (firt_slice_first_mb_in_slice != 0) {    // 第一个片的first_mb_in_slice 必须为0
        LOGD("第一个片的 first_mb_in_slice %d",firt_slice_first_mb_in_slice);
        return false;
    }

    for (int i=0; i<_pic_slice_count; i++) {
        H264SliceHeaderSimpleInfo* info = &_pic_slices[i];
//        NSLog(@"current_frame_index %d _pic_slice_count %d info->frame_num %d first_mb_in_slice %d",frame_index,_pic_slice_count,info->frame_num,info->first_mb_in_slice);
        if (info->frame_num != frame_index) {
            LOGD("此片组的片frame不一致");
            return false;
        }

        // 片的宏块索引必须是增加的，否则说明片出错了
        if (i>0 && info->first_mb_in_slice <= last_slice_first_mb) {
            LOGD("此片组的片first_mb_in_slice 不是递增的");
            return false;
        }

        last_slice_first_mb = info->first_mb_in_slice;
    }

    return true;
}

bool HardDecoder::h265CheckCurentFrame()
{
    return true;
}