//
//  VideoProcess.cpp
//  study
//
//  Created by 飞拍科技 on 2018/12/29.
//  Copyright © 2018 飞拍科技. All rights reserved.
//

#include "VideoProcess.hpp"
#include "CPPLog.h"
#include "CommonDefine.h"

VideoProcess::VideoProcess(VideoType type):fType(type),fFrame(NULL),fCodec_ctx(NULL),fParser_ctx(NULL)
{
    LOGD("VideoProcess");
    _cachedBuf = (uint8_t*)av_mallocz(100*1024);
    _packet_size = 0;
    pthread_mutex_init(&fFrameMutex, NULL);
    pthread_mutex_init(&fCodecMutex, NULL);
    pthread_mutex_init(&fCacheMutex, NULL);
    
    initDecodeContext();
}

VideoProcess::~VideoProcess(){
    destroyDecodeContext();
    pthread_mutex_destroy(&fFrameMutex);
    pthread_mutex_destroy(&fCodecMutex);
    pthread_mutex_destroy(&fCacheMutex);
}

void VideoProcess::initDecodeContext()
{
    AVCodec *pCode = NULL;
    pthread_mutex_lock(&fFrameMutex);
    av_log_set_level(AV_LOG_DEBUG);
    fFrame = av_frame_alloc();
    if (fFrame == NULL) {
        LOGD("av_frame_alloc() fail");
    }
    pthread_mutex_unlock(&fFrameMutex);
    
    pthread_mutex_lock(&fCodecMutex);

    if (fType == VideoTypeH264) {
        pCode = avcodec_find_decoder(AV_CODEC_ID_H264);
    } else if (fType == VideoTypeH265) {
        pCode = avcodec_find_decoder(AV_CODEC_ID_HEVC);
    } else {
        LOGD("not surpot decode type %d",fType);
    }

    if (!pCode) {
        LOGD("find decoder fail");
    } else {
        LOGD("find decoder codeId %d sucess fEnableHardDecode %d",pCode->id,fEnableHardDecode);
    }

    fParser_ctx = av_parser_init(pCode->id);
    fCodec_ctx = avcodec_alloc_context3(pCode);
    if (fCodec_ctx == NULL) {
        LOGD("%s","fCodec_ctx NULL");
    }
//    fCodec_ctx->flags2 |= CODEC_FLAG2_FAST;
//    fCodec_ctx->thread_count = 2;
//    fCodec_ctx->thread_type = FF_THREAD_FRAME;
//    if (pCode->capabilities & CODEC_FLAG_LOW_DELAY) {
//        fCodec_ctx->flags |= CODEC_FLAG_LOW_DELAY;
//    }
    int re = avcodec_open2(fCodec_ctx,pCode,NULL);
    if (re != 0) {
        LOGD("%s %d","avcodec_open2 fail",re);
    }
    LOGD("Init param:%d %d %d %d %d",fCodec_ctx->ticks_per_frame,fCodec_ctx->delay,fCodec_ctx->thread_count,fCodec_ctx->thread_type,fCodec_ctx->active_thread_type);
    pthread_mutex_unlock(&fCodecMutex);
}

void VideoProcess::destroyDecodeContext()
{
    pthread_mutex_lock(&fCacheMutex);
    if (_cachedBuf != NULL) {
        av_free(_cachedBuf);
        _cachedBuf = NULL;
    }
    pthread_mutex_unlock(&fCacheMutex);

    pthread_mutex_lock(&fCodecMutex);

    pthread_mutex_lock(&fFrameMutex);
    if (fFrame != NULL) {
        av_frame_free(&fFrame);
        fFrame = NULL;
    }
    if (fCodec_ctx != NULL) {
        avcodec_free_context(&fCodec_ctx);
        fCodec_ctx = NULL;
    }
    if (fParser_ctx != NULL) {
        av_parser_close(fParser_ctx);
        fParser_ctx = NULL;
    }
    pthread_mutex_unlock(&fFrameMutex);

    pthread_mutex_unlock(&fCodecMutex);
}

void VideoProcess::parseVideo(u_int8_t* beforData,int beforLength,void *callbackClient,ParseNalBlock *callback,
                              bool continueSeq)
{
    if (beforData == NULL || beforLength <= 0) {
        return;
    }

    if (beforData == NULL || beforLength <= 5) {
        return;
    }

    int nalu_type = beforData[4]&0x1F;
//    LOGD("获取到的帧数据 size %d type %d",beforLength,nalu_type);
#if 0
    static int i1=0;
    if (nalu_type == 7) {
        if (i1 >= 1) {
            LOGD("sps 丢失");
            return;
        }
        i1++;
    }
#endif
#if 0
    static int i2=0;
    if (nalu_type == 8) {
        if (i2 >= 1) {
            LOGD("pps 丢失");
            return;
        }
        i2++;
    }
#endif
#if 0
    if (nalu_type == 5) {
        if (random() % 2 == 0) {
            LOGD("i frame 丢失");
            return;
        }
    }
#endif
#if 0
    if (nalu_type == 1) {
        if (random() % 10 == 0) {
            LOGD("p frame 丢失");
            return;
        }
    }
#endif

#if 0
    bool findPacket = false;
    if (nalu_type == 7) {   //sps
        int w,h;
        _packet_size = 0;
        h264_decode_seq_parameter_set_out(beforData+4, beforLength-4, &w, &h, &_sps_fps, &_currentSPS);
        pthread_mutex_lock(&fCacheMutex);
        if (_cachedBuf != NULL) {
            memcpy(_cachedBuf+_packet_size, beforData, beforLength);
        }
        pthread_mutex_unlock(&fCacheMutex);
        _packet_size += beforLength;
        _InNewIdrframe = true;
        _sps_w = w;
        _sps_h = h;

    } else if(nalu_type == 8){  // pps
        if (continueSeq) {  // sps有了
            pthread_mutex_lock(&fCacheMutex);
            if (_cachedBuf != NULL) {
                memcpy(_cachedBuf+_packet_size, beforData, beforLength);
            }
            pthread_mutex_unlock(&fCacheMutex);
            _packet_size += beforLength;
        } else {
            _packet_size = 0;
        }

        _InNewIdrframe = true;
    } else if (nalu_type == 1 || nalu_type == 5) {
        if (_InNewIdrframe) {
            if (nalu_type == 1) {
                LOGD("idr 丢失了");
                findPacket = false;
                _packet_size = 0;
                _waitNextIdrframe = true;
            } else {
                findPacket = true;
                _InNewIdrframe = false;
                _waitNextIdrframe = false;
                pthread_mutex_lock(&fCacheMutex);
                if (_cachedBuf != NULL) {
                    memcpy(_cachedBuf+_packet_size, beforData, beforLength);
                }
                pthread_mutex_unlock(&fCacheMutex);
                _packet_size += beforLength;
            }
        } else {
            if (!_waitNextIdrframe) {
                findPacket = true;
                pthread_mutex_lock(&fCacheMutex);
                if (_cachedBuf != NULL) {
                    memcpy(_cachedBuf+_packet_size, beforData, beforLength);
                }
                pthread_mutex_unlock(&fCacheMutex);
                _packet_size += beforLength;
            } else {
                findPacket = false;
                LOGD("等待下一个idr frame");
            }
        }
    }

    if (findPacket) {

        if (callbackClient != NULL && callback != NULL && _packet_size>0) {

            // 将视频数据组装成 VideoPacket格式的结构返回
            int packetSize = _packet_size;
            VideoPacket *outputPacket = (VideoPacket*)malloc(sizeof(VideoPacket) + packetSize);
            memset(outputPacket, 0, sizeof(VideoPacket));
            outputPacket->frameType = fType;
            outputPacket->frameSize = packetSize;
            outputPacket->frameWidth = _sps_w;
            outputPacket->frameHeight = _sps_h;
            pthread_mutex_lock(&fCacheMutex);
            if (_cachedBuf != NULL) {
                memcpy(outputPacket+1, _cachedBuf, packetSize);
            }
            pthread_mutex_unlock(&fCacheMutex);

            callback(callbackClient,outputPacket);
        }

//        LOGD("一帧视频 大小 %d",_packet_size);
        _packet_size = 0;
    }

#else

    // 此步骤必须要，否则有可能奔溃
    size_t lengthWithPadding = beforLength + AV_INPUT_BUFFER_PADDING_SIZE;
    uint8_t *bufferWithPadding = (uint8_t*)malloc(lengthWithPadding);
    memset(bufferWithPadding, 0, lengthWithPadding);
    memcpy(bufferWithPadding, beforData, beforLength);
    uint8_t *bufferLeft = bufferWithPadding;
    int lengthLeft = beforLength;
    int parseLength = 0;

    while (lengthLeft > 0) {
        bool findPacket = false;
        AVPacket packet;
        av_init_packet(&packet);
        int width = 0;
        int height = 0;
        pthread_mutex_lock(&fCodecMutex);
        do {
            if (fCodec_ctx == NULL || fParser_ctx == NULL) {
                break;
            }

            parseLength = av_parser_parse2(fParser_ctx,
                                           fCodec_ctx,
                                           &packet.data,
                                           &packet.size,
                                           bufferLeft,
                                           lengthLeft,
                                           AV_NOPTS_VALUE,
                                           AV_NOPTS_VALUE,
                                           AV_NOPTS_VALUE);
            bufferLeft += parseLength;
            lengthLeft -= parseLength;
            if (packet.size <= 0) { // 说明还没有解析到一个完整的包
                break;
            }
            findPacket = true;
            width = fParser_ctx->width;
            height = fParser_ctx->height;
        } while (false);
        pthread_mutex_unlock(&fCodecMutex);

//        if (findPacket) {
//            LOGD("读取到包 大小 %d left ==>%d keyframe %d output_picture_number %d",packet.size,lengthLeft,fParser_ctx->key_frame,fParser_ctx->output_picture_number);
//        }

        if(findPacket) {
            // 将视频数据组装成 VideoPacket格式的结构返回
            int packetSize = packet.size;
            VideoPacket *outputPacket = (VideoPacket*)malloc(sizeof(VideoPacket) + packetSize);
            memset(outputPacket, 0, sizeof(VideoPacket));

            outputPacket->frameType = fType;
            outputPacket->frameSize = packetSize;
            outputPacket->frameWidth = width;
            outputPacket->frameHeight = height;
            memcpy(outputPacket+1, packet.data, packetSize);

            if (callbackClient != NULL && callback != NULL) {
                callback(callbackClient,outputPacket);
            } else {
                // 确保没用的内存释放
                if (outputPacket != NULL) {
                    free(outputPacket);
                    outputPacket = NULL;
                }
            }
        }

        av_packet_unref(&packet);
    }

    if (bufferWithPadding != NULL) {
        free(bufferWithPadding);
        bufferWithPadding = NULL;
    }
#endif
}

bool VideoProcess::decodeVideo(uint8_t* compressedData,int length)
{
    if (compressedData == NULL || length <= 4) {
        LOGD("要解码的数据不正确");
        return false;
    }
    
    AVPacket packet;
    av_init_packet(&packet);
    packet.data = compressedData;
    packet.size = length;
    
    pthread_mutex_lock(&fCodecMutex);

    pthread_mutex_lock(&fFrameMutex);
    int re_ret1 = avcodec_send_packet(fCodec_ctx, &packet);
    int re_ret2 = avcodec_receive_frame(fCodec_ctx,fFrame);
    if (re_ret1 != 0 || re_ret2 != 0) {
        LOGD("解码失败 re_ret1 %d re_ret2 %d",re_ret1,re_ret2);
    }
//    LOGD("key frame %d pict_type %d AVPixelFormat %d size %d",fFrame->key_frame,fFrame->pict_type,fFrame->format,packet.size);
    fWidth = fFrame->width;
    fHeigth = fFrame->height;
    pthread_mutex_unlock(&fFrameMutex);
    
    pthread_mutex_unlock(&fCodecMutex);
    
    av_packet_unref(&packet);
    
    return re_ret1 == 0 && re_ret2 == 0;
}

void VideoProcess::getYUVFrame(VideoFrameYUV *yuvFrame)
{
    pthread_mutex_lock(&fFrameMutex);
    do{
        if (!fFrame) {
            break;
        }
        
        int frameWidth = fFrame->width;
        int frameHeight = fFrame->height;
        int line_size[3] = {0};
        line_size[0] = fFrame->linesize[0];
        line_size[1] = fFrame->linesize[1];
        line_size[2] = fFrame->linesize[2];
        
        if (yuvFrame->luma != NULL && (yuvFrame->width != frameWidth || yuvFrame->height != frameHeight)) {
            free(yuvFrame->luma);
            free(yuvFrame->chromaB);
            free(yuvFrame->chromaR);
            
            yuvFrame->luma = NULL;
            yuvFrame->chromaB = NULL;
            yuvFrame->chromaR = NULL;
        }
        if(yuvFrame->luma == NULL){
            yuvFrame->luma = (uint8_t*) malloc(frameWidth * frameHeight);
            yuvFrame->chromaB = (uint8_t*) malloc(frameWidth * frameHeight/4);
            yuvFrame->chromaR = (uint8_t*) malloc(frameWidth * frameHeight/4);
        }
        
        CPY_YUV_FRAME(yuvFrame->luma, fFrame->data[0], fFrame->linesize[0], frameWidth, frameHeight);
        CPY_YUV_FRAME(yuvFrame->chromaB, fFrame->data[1], fFrame->linesize[1], frameWidth/2, frameHeight/2);
        CPY_YUV_FRAME(yuvFrame->chromaR, fFrame->data[2], fFrame->linesize[2], frameWidth/2, frameHeight/2);
        
        yuvFrame->frameType = VideFrameTypeYUV420Planer;
        yuvFrame->width = frameWidth;
        yuvFrame->height = frameHeight;
        
    } while (false);
    pthread_mutex_unlock(&fFrameMutex);
}
