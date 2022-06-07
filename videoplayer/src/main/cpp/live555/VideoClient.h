//
//  RtspClient.hpp
//  study
//
//  Created by study on 16/8/16.
//  Copyright © 2016年 study. All rights reserved.
//

#ifndef VideoClient_hpp
#define VideoClient_hpp

#include <stdio.h>
#include <stdlib.h>
#include "liveMedia.hh"
#include "RTSPClient.hh"
#include "BasicUsageEnvironment.hh"
#include "CommonDefine.h"

enum ConnectStatus{
    ConnectFail = -1,//请求超时
    ConnectSucess = 0,
    ConnectServerHasShutdown = 1    // rtsp服务器已经关闭了，需要重启相机
};

typedef void (receiveVideoFrameDataHandler)(void* client,ConnectStatus success,VideoType type,u_int8_t* video,int size,
                                            bool continueSeq);

class StreamClientState {
public:
    StreamClientState();
    virtual ~StreamClientState();

public:
    MediaSubsessionIterator* iter;
    MediaSession* session;
    MediaSubsession* subsession;
    TaskToken streamTimerTask;
    TaskToken timeoutTask;
    double duration;
};

class VideoClient: public RTSPClient {
public:
    VideoClient(UsageEnvironment& env, char const* rtspUR);
    virtual ~VideoClient();

    void startStream();
    void shuttStream();
    void pauseStream();
    void resumeStream();
    StreamClientState scs;
    void *client;
    receiveVideoFrameDataHandler *frameHandler;
private:
    VideoType type;
};

#endif


