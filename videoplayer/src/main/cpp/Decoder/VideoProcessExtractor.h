//
// Created by 飞拍科技 on 2019/2/28.
//

#ifndef FLYPIEFORANDROID_VIDEOPROCESSEXTRACTOR_H
#define FLYPIEFORANDROID_VIDEOPROCESSEXTRACTOR_H

#include "VideoClient.h"
#include "VideoProcess.hpp"
#include "PacketQueue.h"
#include "HardDecoder.h"
#include "SoftDecoder.h"
#include <vector>

#define CACHE_YUV_FRAME_NUMBER (2)
typedef enum {
    DecodeStatusHasNodata,
    DecodeStatusFialure,
    DecodeStatusSucess
}DecodeStatus;

/** 对视频进行解析和解码
 * 该类必须是线程安全的，因为最好的是它能工作在两个不同的线程
 * 输送原始数据 --> 解析成avpacket(线程1) --> 放入缓冲区
 * 从缓冲区获取数据(线程2) -->睡眠或者解码(若无数据，则睡眠直到有数据被唤醒)
 * 线程由java层来创建和管理，本类只实现解析，解码，和数据缓存提取逻辑；
 * */
class VideoProcessExtractor
{
public:
    VideoProcessExtractor(bool enableHard = true);
    virtual ~VideoProcessExtractor();

    //  由外界输送原始数据
    void pushOrignalVideoData(uint8_t *data,int length,VideoType type, bool pushOrignalVideoData);
    void clearVideoData();
    void weakeupa();

    // 设置是否开启硬解码,默认开启
    void enableHardDecode(bool enable = true);

    void setRenderWindow(ANativeWindow *window);
    // 解码完成后的回调
    typedef void DecodeCallback(void* client,VideoFrameYUV *yuvFrame);
    void setDecodeCallback(void *delegate,DecodeCallback *cb);
    // 解码 同步函数
    VideoFrameYUV* decode();
private:

    // 当前的视频解析解 默认处理h264
    VideoProcess *fVideoParser;

    // 解析完成后的回调
    static void paseCallback(void *mySelf,VideoPacket *packet);
    // 解析
    void parse(uint8_t *data, int length,bool continueSeq);
    void resetParser(VideoType type);

    DecodeCallback *fDecodeCallback;
    void *fDelegate;

    bool fEnableHardDecode;
    // 当前的视频解码器 默认处理h264
    Decoder *fVideoDecoder;

    void resetDecoder(VideoType type);

    // 用于比较码流编码类型是否发生了变化 默认h264
    VideoType fCurrentVideoType;

    // 缓冲区 用于存储解析后的完整数据帧(avpacket)
    PacketQueue *fDataQueue;

    // 锁
    pthread_mutex_t fMutex;

};
#endif //FLYPIEFORANDROID_VIDEOPROCESSEXTRACTOR_H
