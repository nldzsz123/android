//
//  VideoProcess.hpp
//  study
//
//  Created by 飞拍科技 on 2018/12/29.
//  Copyright © 2018 飞拍科技. All rights reserved.
//

#ifndef VideoProcess_hpp
#define VideoProcess_hpp

#include <stdio.h>
#include <pthread.h>

extern "C" {
    #include "libavcodec/avcodec.h"
    #include "VideoHelper.h"
}

#include "CPPLog.h"
#include "CommonDefine.h"

/**
 *  定义用于处理视频的抽象类，包括编码和解码两部分，该类的对象所具有的功能为：
 *  1、对视频数据进行NAL分割
 *  2、解码视频数据
 *  3、输出解码视频的类型，比如对于H264，可以类型有I帧，P帧等等。
 *  4、需要ffmpeg的支持
 */
class VideoProcess{
public:
    virtual ~VideoProcess();
    VideoProcess(VideoType type);
    
    // 解析成功一个NAL包后的回调
    typedef void ParseNalBlock(void *client,VideoPacket *packet);
    // 对视频数据进行分割和解析，找到一个NAL单元，可能调用多次才会解析到一个NAL，data为要解析的视频数据
    void parseVideo(u_int8_t* data,int length,void *callbackClient,ParseNalBlock *callback, bool continueSeq);
    
    // 解码视频 decodeVideo 函数解码视频 getYUVFrame获取解码后的YUV数据
    bool decodeVideo(uint8_t* compressedData,int length);
    void getYUVFrame(VideoFrameYUV* frame);
    
    // 解码器的类型
    VideoType decodeType(){return fType;};
    int videoWidth() {return fWidth;};
    int videoHeight() {return fHeigth;};
    int videofps() {return fFps;};
    
protected:
    pthread_mutex_t fCodecMutex;    // 用于解析的锁
    pthread_mutex_t fFrameMutex;    // 用于解码的锁
    pthread_mutex_t fCacheMutex;    // 用于缓冲区操作的锁

    //++++++ ffmpeg ++++++++//
    AVFrame                 *fFrame;
    AVCodecContext          *fCodec_ctx;
    AVCodecParserContext    *fParser_ctx;
    
private:
    VideoType fType;
    bool fEnableHardDecode;
    int fWidth;
    int fHeigth;
    int fFps;
    
    // 初始化和销毁解码上下文，分别用于构造和析构函数中
    void initDecodeContext();
    void destroyDecodeContext();

    SPS _currentSPS;        // SPS的解析
    int _sps_w;             // SPS中视频的宽
    int _sps_h;             // SPS中视频的高
    int _sps_fps;           // SPS中的帧率

    int _packet_size;
    u_int8_t*   _cachedBuf;

    bool    _InNewIdrframe;
    bool    _waitNextIdrframe;
};
#endif /* VideoProcess_hpp */
