//
// Created by 飞拍科技 on 2019/3/2.
//

#ifndef FLYPIEFORANDROID_HARDDECODER_H
#define FLYPIEFORANDROID_HARDDECODER_H

#include "Deocder.h"
#include "media/NdkMediaCodec.h"
#include "media/NdkMediaFormat.h"
#include "CPPLog.h"
#include <pthread.h>

extern "C"{
#include  "VideoHelper.h"
#include "libavutil/avutil.h"
}

#define PPS_SPS_MAX_SIZE (256)
#define NALU_MAX_SIZE (1*1024*1024)
#define AUD_MAX_SIZE (2*1024*1024)
#define FRAME_MAX_SLICE_COUNT (32) // 根据h264文档定义，一个视频帧分片组中，分片的个数不能超过32个

/** 继承方式 public protected private
 * 使用public继承,父类中的方法属性不发生改变;
 * 使用protected继承,父类的protected和public方法在子类中变为protected,private方法不变;
 * 使用private继承,父类的所有方法在子类中变为private;
 * */
class HardDecoder:public Decoder {
public:
    HardDecoder(VideoType type);
    virtual ~HardDecoder();

    virtual void setDecoderType(VideoType type);
    void setWidthAndHeight(int width,int height);

    // 此方法和decode方法在同一线程调用
    virtual void setRenderWindow(ANativeWindow *window);
    // 表示实现父类方法
    virtual VideoFrameYUV* decode(uint8_t* compressedData,int length);


    void resetDecoder();

    const uint8_t naluStart4Byte1[4] = {0, 0, 0, 1};
    const uint8_t naluStart3Byte1[3] = {0, 0, 1};
private:
    // 前面函数返回类型如果是bool 会造成奔溃，不解！
    void destroyDecoderContext();

    // 硬解码器
    AMediaCodec *fCodec;
    AMediaFormat *fFormat;
    VideoType fType;
    ANativeWindow *fWindow;

    pthread_mutex_t fMutex; // 解码用的锁


    uint8_t sps_buffer[PPS_SPS_MAX_SIZE];
    uint8_t pps_buffer[PPS_SPS_MAX_SIZE];
    uint8_t vps_buffer[PPS_SPS_MAX_SIZE];
    int pps_size,sps_size,vps_size;

    // 当nal的码流分隔符为001时，由于videotoolbox只能已0001开头的，所以需要将001替换成0001，这里创建一个缓存来实现，避免重复创建
    uint8_t *nalu_buffer;

    // 真正用于进行解码的视频缓存，一个aud_buf 可以包含多个nal，由是将avpacket中的nal_unit_type 为1，2，3，4，5提取出来重新组合在一起，送给解码器解码
    uint8_t *aud_buffer;
    int aud_size;
    int aud_nal_count;

    //264 context, for verification 246 stream.
    SPS _currentSPS;        // SPS的解析
    int _sps_w;             // SPS中视频的宽
    int _sps_h;             // SPS中视频的高
    int _sps_fps;           // SPS中的帧率
    int _pic_slice_count;   // 一帧图像中片的数目
    H264SliceHeaderSimpleInfo _pic_slices[FRAME_MAX_SLICE_COUNT];

    bool fDecodeInited;
    bool fPushedIFrame;
    bool fHardwareSupport;
    bool fDecodeErrorNum;
    int  fCreate_decoder_count;

    // 初始化解码器,decode() 调用之前必须先初始化
    bool doInitDecoder();
    void setNeedsReset();
    void clearSliceAnaContex();
    bool doInitMediaCodecSessionWithProps();
    bool decodeInitWithSingleNAL(uint8_t* data, int size);
    bool h264InitWithSingleNAL(uint8_t *data, int size);
    bool h265InitWithSingleNAL(uint8_t *data, int size);
    bool nalAnalysis(uint8_t* data,int size);
    bool h264NalAnalysis(uint8_t* data, int size);
    bool h265NalAnalysis(uint8_t* data, int size);
    void sliceProcess(uint8_t* data, int size);
    bool loadIdrFrame(uint8_t *data, int size);
    uint8_t* decodeWithBuffer(uint8_t *data, int size);

    bool canDecode();
    bool checkCurrentFrame();
    bool h264CheckCurentFrame();
    bool h265CheckCurentFrame();
};

#endif //FLYPIEFORANDROID_HARDDECODER_H
