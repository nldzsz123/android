//
// Created by 飞拍科技 on 2019/3/2.
//

#ifndef FLYPIEFORANDROID_SOFTDECODER_H
#define FLYPIEFORANDROID_SOFTDECODER_H

#include "Deocder.h"
#include "VideoProcess.hpp"
#define CACHE_YUV_FRAME_NUMBER (2)

class SoftDecoder: public Decoder {
public:
    SoftDecoder(VideoType type);
    ~SoftDecoder();

    virtual void setDecoderType(VideoType type);
    virtual void setRenderWindow(ANativeWindow *window);
    virtual VideoFrameYUV* decode(uint8_t* compressedData,int length);
private:

    // 当前的视频解码器 默认处理h264
    VideoProcess *fVideoDecoder;
    // 避免频繁的创建和销毁内存，创建一个保存yuvFrame的缓存区
    VideoFrameYUV *fYUVFrames[CACHE_YUV_FRAME_NUMBER];
    // 目前使用的yuvFrame的缓存中的索引
    int fCurrentIndex;

    VideoType fType;

    void resetDecoder(VideoType type);
};

#endif //FLYPIEFORANDROID_SOFTDECODER_H
