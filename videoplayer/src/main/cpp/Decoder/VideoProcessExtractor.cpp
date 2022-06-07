//
// Created by 飞拍科技 on 2019/2/28.
//

#include <ffmpegExtractor/CommonDefine.h>
#include "VideoProcessExtractor.h"

// 指针类变量 最好是都初始化，默认值不会是NULL(非常重要)
VideoProcessExtractor::VideoProcessExtractor(bool enableHard):fEnableHardDecode(enableHard),fVideoParser(NULL),fVideoDecoder(NULL),fDecodeCallback(NULL),
fDelegate(NULL),fCurrentVideoType(VideoTypeH264),fDataQueue(NULL)
{
    LOGD("VideoProcessExtractor()");
    pthread_mutex_init(&fMutex, NULL);
    resetParser(VideoTypeH264);
    resetDecoder(VideoTypeH264);
    fDataQueue = new PacketQueue(100);
}

VideoProcessExtractor::~VideoProcessExtractor()
{
    LOGD("~ VideoProcessExtractor()");
    if (fDataQueue != NULL) {
        fDataQueue->clear();
        delete fDataQueue;
        fDataQueue = NULL;
    }

    if (fVideoParser) {
        delete fVideoParser;
        fVideoParser = NULL;
    }
    if (fVideoDecoder) {
        delete fVideoDecoder;
        fVideoDecoder = NULL;
    }
    pthread_mutex_destroy(&fMutex);
}

void VideoProcessExtractor::pushOrignalVideoData(uint8_t *data, int length, VideoType type, bool continueSeq)
{
    if (data == NULL) {
        LOGD("data is NULL return");
        return;
    }

    // 发现码流的编码器更换了，则这里也及时更换
    if (fCurrentVideoType != type) {
        LOGD("resetParser fCurrentVideoType %d new type %d",fCurrentVideoType,type);
        fCurrentVideoType = type;
        fDataQueue->clear();
        resetParser(type);
    }

    parse(data,length,continueSeq);
}

void VideoProcessExtractor::clearVideoData()
{
    if (fDataQueue != NULL) {
        LOGD("清楚所有视频数据缓存");
        fDataQueue->clear();
    }
}

void VideoProcessExtractor::weakeupa()
{
    if (fDataQueue != NULL) {
        LOGD("唤醒 锁");
        fDataQueue->wakeUp();
    }
}

void VideoProcessExtractor::enableHardDecode(bool enable)
{
    if (fEnableHardDecode == enable) {
        return;
    }
    fEnableHardDecode = enable;
    resetDecoder(fCurrentVideoType);
}
void VideoProcessExtractor::setRenderWindow(ANativeWindow *window)
{
    fVideoDecoder->setRenderWindow(window);
}
void VideoProcessExtractor::setDecodeCallback(void *delegate,DecodeCallback *cb)
{
    fDelegate = delegate;
    fDecodeCallback = cb;
}

// 该函数有可能运行在不同的线程，所以需要注意线程安全
VideoFrameYUV* VideoProcessExtractor::decode()
{
    VideoPacket *packet = NULL;
    int packetSize = 0;
    uint8_t *videoPacket = NULL;
    int videoPacketLength;
    packet = (VideoPacket*)fDataQueue->pullData(&packetSize);
    if (packet && packetSize == packet->frameSize + sizeof(VideoPacket)) {
        videoPacket = packet->frameData;
        videoPacketLength = packet->frameSize;
    }

    if (videoPacket == NULL) {  //说明暂时没有视频数据
        LOGD("没有数据");
        return NULL;
    }

    // 检查编码器的编码方式
    VideoType type = (VideoType)packet->frameType;
    if (fCurrentVideoType != type) {
        resetDecoder(type);
    }

    VideoFrameYUV *yuvFrame = fVideoDecoder->decode(videoPacket,videoPacketLength);

    if (packet) {
        free(packet);
    }
    return yuvFrame;
}

void VideoProcessExtractor::paseCallback(void *mySelf, VideoPacket *packet)
{
    VideoProcessExtractor *thisSelf = (VideoProcessExtractor*)mySelf;
    if (packet == NULL) {
        LOGD("解析失败");
        return;
    }

    if (thisSelf->fDataQueue->count() > 70) {
        LOGD("缓存区数据已经满了");
        thisSelf->fDataQueue->clear();
    } else {
        thisSelf->fDataQueue->addData((uint8_t*)packet,sizeof(VideoPacket)+packet->frameSize);
    }
}

void VideoProcessExtractor::parse(uint8_t *data, int length, bool continueSeq)
{
    fVideoParser->parseVideo(data,length,this,VideoProcessExtractor::paseCallback,continueSeq);
}

void VideoProcessExtractor::resetParser(VideoType type)
{
    if (fVideoParser != NULL) {
        delete fVideoParser;
        fVideoParser = NULL;
    }
    fVideoParser = new VideoProcess(type);
}

void VideoProcessExtractor::resetDecoder(VideoType type)
{
    if (fVideoDecoder != NULL) {
        delete fVideoDecoder;
        fVideoDecoder = NULL;
    }
    if (fEnableHardDecode) {
        fVideoDecoder = new HardDecoder(type);
    } else {
        fVideoDecoder = new SoftDecoder(type);
    }
}
