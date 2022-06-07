//
//  RtspClient.cpp
//  study
//
//  Created by study on 16/8/16.
//  Copyright © 2016年 study. All rights reserved.
//

#include "VideoClient.h"
#include "CPPLog.h"

#define DUMMY_SINK_RECEIVE_BUFFER_SIZE (4*1024*1024)
#define TimeOut (6*1000000)

void continueAfterDESCRIBE(RTSPClient* rtspClient, int resultCode, char* resultString);
void continueAfterSETUP(RTSPClient* rtspClient, int resultCode, char* resultString);
void continueAfterPLAY(RTSPClient* rtspClient, int resultCode, char* resultString);
void continueAfterPause(RTSPClient* rtspClient, int resultCode, char* resultString);
void subsessionAfterPlaying(void* clientData);
void subsessionByeHandler(void* clientData);
void streamTimerHandler(void* clientData);
#define timeout 1
#if timeout
void timeoutHandler(void *clientData);
void needRebootHandler(void *clientData);
void sendDescribFunction(void *clientData);
#endif
void openURL(UsageEnvironment& env, char const* progName, char const* rtspURL);
void setupNextSubsession(RTSPClient* rtspClient);
void shutdownStream(RTSPClient* rtspClient, int exitCode = 1);

class DummySink: public MediaSink {
public:
    static DummySink* createNew(UsageEnvironment& env, MediaSubsession& subsession, char const* streamId = NULL);

private:
    DummySink(UsageEnvironment& env, MediaSubsession& subsession, char const* streamId);
    virtual ~DummySink();

    static void afterGettingFrame(void* clientData, unsigned frameSize,
                                  unsigned numTruncatedBytes,
                                  struct timeval presentationTime,
                                  unsigned durationInMicroseconds);
    void afterGettingFrame(unsigned frameSize, unsigned numTruncatedBytes,
                           struct timeval presentationTime, unsigned durationInMicroseconds);

private:
    virtual Boolean continuePlaying();  // 表示重载了父类的函数 父类的这个函数也可能是纯虚函数

private:
    u_int8_t* fReceiveBuffer;
    MediaSubsession& fSubsession;
    char* fStreamId;
private:
    u_int8_t const* sps;
    unsigned spsSize;
    u_int8_t const* pps;
    unsigned ppsSize;

public:	void setSprop(u_int8_t const* prop, unsigned size);
};

#pragma mark ourRTSPClient 类实现
VideoClient::VideoClient(UsageEnvironment& env, char const* rtspURL)
        : RTSPClient(env,rtspURL, 0, NULL, -1)
{
    type = VideoTypeH264;
}

VideoClient::~VideoClient() {

}

void VideoClient::startStream()
{
    this->sendDescribeCommand(continueAfterDESCRIBE);
#if timeout     // 6秒钟没有回应 则认为相机已经死掉
    this->scs.timeoutTask = this->envir().taskScheduler().scheduleDelayedTask(TimeOut, (TaskFunc*)needRebootHandler, this);
#endif
}

void VideoClient::shuttStream()
{
    shutdownStream(this);
}

void VideoClient:: pauseStream()
{
    StreamClientState& scs = ((VideoClient*)this)->scs;
    if (scs.session != NULL) {
        this->sendPauseCommand(*scs.session, NULL);
    }
}
void VideoClient:: resumeStream()
{
    StreamClientState& scs = ((VideoClient*)this)->scs;
    scs.subsession->sink->startPlaying(*(scs.subsession->readSource()),
                                       subsessionAfterPlaying, scs.subsession);

    if (scs.subsession->rtcpInstance() != NULL) {
        scs.subsession->rtcpInstance()->setByeHandler(subsessionByeHandler, scs.subsession);
    }

}
#pragma mark StreamClientState 类实现
StreamClientState::StreamClientState()
        : iter(NULL), session(NULL), subsession(NULL), streamTimerTask(NULL), duration(0.0) {
}

StreamClientState::~StreamClientState() {
#ifdef DDEBUG
    printf("~StreamClientState");
#endif
    delete iter;
    if (session != NULL) {
        UsageEnvironment& env = session->envir();

        env.taskScheduler().unscheduleDelayedTask(streamTimerTask);
        env.taskScheduler().unscheduleDelayedTask(timeoutTask);
        Medium::close(session);
    }
}

#pragma mark DummySink 类实现
DummySink* DummySink::createNew(UsageEnvironment& env, MediaSubsession& subsession, char const* streamId) {
    return new DummySink(env, subsession, streamId);
}

DummySink::DummySink(UsageEnvironment& env, MediaSubsession& subsession, char const* streamId)
        : MediaSink(env),
          fSubsession(subsession) {
    fStreamId = strDup(streamId);
    fReceiveBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
}

DummySink::~DummySink() {
    delete[] fReceiveBuffer;
    fReceiveBuffer = NULL;
    delete[] fStreamId;
}

void DummySink::setSprop(u_int8_t const* prop, unsigned size) {
    uint8_t *buf;
    buf = (uint8_t *)malloc(size);
    memcpy (buf, prop, size);

    VideoClient* rtspClient = (VideoClient*)fSubsession.miscPtr;

    if (rtspClient != NULL && rtspClient->frameHandler != NULL && fSubsession.codecName() != NULL) {
        VideoType type = VideoTypeH264;
        if (strcmp(fSubsession.codecName(), "H265") == 0) {
            type = VideoTypeH265;
        } else if (strcmp(fSubsession.codecName(), "H264") == 0){
            type = VideoTypeH264;
        }
        rtspClient->frameHandler(rtspClient,ConnectSucess,type, buf,size, true);
    }
}

void DummySink::afterGettingFrame(void* clientData, unsigned frameSize, unsigned numTruncatedBytes,
                                  struct timeval presentationTime, unsigned durationInMicroseconds) {
    DummySink* sink = (DummySink*)clientData;
    sink->afterGettingFrame(frameSize, numTruncatedBytes, presentationTime, durationInMicroseconds);
}

void DummySink::afterGettingFrame(unsigned frameSize, unsigned numTruncatedBytes,
                                  struct timeval presentationTime, unsigned /*durationInMicroseconds*/) {

    VideoClient* rtspClient = (VideoClient*)fSubsession.miscPtr;
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
    scs.subsession = scs.iter->next();
#if timeout
    rtspClient->envir().taskScheduler().rescheduleDelayedTask(scs.timeoutTask, TimeOut, (TaskFunc*)timeoutHandler, rtspClient);
#endif

    UsageEnvironment& env = rtspClient->envir();

    if (rtspClient->frameHandler != NULL && fSubsession.codecName() != NULL) {
        VideoType type = VideoTypeH264;
        if (strcmp(fSubsession.codecName(), "H265") == 0) {
            type = VideoTypeH265;
        } else if (strcmp(fSubsession.codecName(), "H264") == 0){
            type = VideoTypeH264;
        }
        bool continueSeq = fSource->continueSeq;
        rtspClient->frameHandler(rtspClient,ConnectSucess,type, fReceiveBuffer,frameSize,continueSeq);
    }

    continuePlaying();
}

Boolean DummySink::continuePlaying() {
    if (fSource == NULL) return False;

    fSource->getNextFrame(fReceiveBuffer, DUMMY_SINK_RECEIVE_BUFFER_SIZE,
                          afterGettingFrame, this,
                          onSourceClosure, this);
    return True;
}

void continueAfterDESCRIBE(RTSPClient* rtspClient, int resultCode, char* resultString) {
    do {
        VideoClient *rtsp = (VideoClient *)rtspClient;
        UsageEnvironment& env = rtspClient->envir();
        StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
#if timeout
        env.taskScheduler().rescheduleDelayedTask(scs.timeoutTask, TimeOut, (TaskFunc*)timeoutHandler, rtsp);
#endif
        if (resultCode != 0) {
            LOGD("Failed to get a SDP description: %s",resultString);
            delete[] resultString;

            // 重试 100毫秒
//            env.taskScheduler().rescheduleDelayedTask(scs.timeoutTask, 100000, (TaskFunc*)sendDescribFunction, rtsp);
            needRebootHandler(rtsp);
            break;
        }

        char* const sdpDescription = resultString;
//        LOGD("Got a SDP description:\n %s",sdpDescription);

        scs.session = MediaSession::createNew(env, sdpDescription);
        delete[] sdpDescription;
        if (scs.session == NULL) {
            LOGD("Failed to create a MediaSession object from the SDP description: %s",env.getResultMsg());
            break;
        } else if (!scs.session->hasSubsessions()) {
            env <<  "This session has no media subsessions (i.e., no \"m=\" lines)\n";
            break;
        }

        scs.iter = new MediaSubsessionIterator(*scs.session);
        setupNextSubsession(rtspClient);
        return;
    } while (0);

    shutdownStream(rtspClient);
}

void setupNextSubsession(RTSPClient* rtspClient) {
//    UsageEnvironment& env = rtspClient->envir();
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;

    scs.subsession = scs.iter->next();
    if (scs.subsession != NULL) {
        if (!scs.subsession->initiate()) {
//            env << "Failed to initiate the subsession: " << env.getResultMsg() << "\n";
            setupNextSubsession(rtspClient);
        } else {
//            env <<  "Initiated the subsession (";
            if (scs.subsession->rtcpIsMuxed()) {
//                env << "client port " << scs.subsession->clientPortNum();
            } else {
//                env << "client ports " << scs.subsession->clientPortNum() << "-" << scs.subsession->clientPortNum()+1;
            }
//            env << ")\n";

            rtspClient->sendSetupCommand(*scs.subsession, continueAfterSETUP, False, False);
        }
        return;
    }

    if (scs.session->absStartTime() != NULL) {

        rtspClient->sendPlayCommand(*scs.session, continueAfterPLAY, scs.session->absStartTime(), scs.session->absEndTime());
    } else {
        scs.duration = scs.session->playEndTime() - scs.session->playStartTime();
        rtspClient->sendPlayCommand(*scs.session, continueAfterPLAY);
    }
}

void continueAfterSETUP(RTSPClient* rtspClient, int resultCode, char* resultString) {
    do {
        VideoClient *rtsp = (VideoClient *)rtspClient;
        UsageEnvironment& env = rtspClient->envir();
        StreamClientState& scs = rtsp->scs;
#if timeout
        env.taskScheduler().rescheduleDelayedTask(scs.timeoutTask, TimeOut, (TaskFunc*)timeoutHandler, rtsp);
#endif
        if (resultCode != 0) {
//            env << "Failed to set up the subsession: " << resultString << "\n";
            break;
        }


//        env << "Set up the subsession (";
        if (scs.subsession->rtcpIsMuxed()) {
//            env << "client port " << scs.subsession->clientPortNum();
        } else {
//            env << "client ports " << scs.subsession->clientPortNum() << "-" << scs.subsession->clientPortNum()+1;
        }
//        env << ")\n";

        const char *sprop = scs.subsession->fmtp_spropparametersets();
        uint8_t const* sps = NULL;
        unsigned spsSize = 0;
        uint8_t const* pps = NULL;
        unsigned ppsSize = 0;

        if (sprop != NULL) {
            unsigned numSPropRecords;
            SPropRecord* sPropRecords = parseSPropParameterSets(sprop, numSPropRecords);
            for (unsigned i = 0; i < numSPropRecords; ++i) {
                if (sPropRecords[i].sPropLength == 0) continue;
                u_int8_t nal_unit_type = (sPropRecords[i].sPropBytes[0])&0x1F;
                if (nal_unit_type == 7) {
                    sps = sPropRecords[i].sPropBytes;
                    spsSize = sPropRecords[i].sPropLength;
                } else if (nal_unit_type == 8) {
                    pps = sPropRecords[i].sPropBytes;
                    ppsSize = sPropRecords[i].sPropLength;
                }
            }
        }

        scs.subsession->sink = DummySink::createNew(env, *scs.subsession, rtspClient->url());
        if (scs.subsession->sink == NULL) {
            env <<"Failed to create a data sink for the \""
                << "\" subsession: " << env.getResultMsg() << "\n";
            break;
        }

//        env << "Created a data sink for the \"" << "\" subsession\n";
        scs.subsession->miscPtr = rtspClient;
        if (sps != NULL) {
//            ((DummySink *)scs.subsession->sink)->setSprop(sps, spsSize);
        }
        if (pps != NULL) {
//            ((DummySink *)scs.subsession->sink)->setSprop(pps, ppsSize);
        }
        scs.subsession->sink->startPlaying(*(scs.subsession->readSource()),
                                           subsessionAfterPlaying, scs.subsession);

        if (scs.subsession->rtcpInstance() != NULL) {
            scs.subsession->rtcpInstance()->setByeHandler(subsessionByeHandler, scs.subsession);
        }
    } while (0);
    delete[] resultString;

    setupNextSubsession(rtspClient);
}

void continueAfterPLAY(RTSPClient* rtspClient, int resultCode, char* resultString) {
    Boolean success = False;

    do {
        UsageEnvironment& env = rtspClient->envir();
        StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
        VideoClient *rtsp = (VideoClient *)rtspClient;
#if timeout
        env.taskScheduler().rescheduleDelayedTask(scs.timeoutTask, TimeOut, (TaskFunc*)timeoutHandler, rtsp);
#endif
        if (resultCode != 0) {
            env << "Failed to start playing session: " << resultString << "\n";
            break;
        }

        if (scs.duration > 0) {
            unsigned const delaySlop = 2;
            scs.duration += delaySlop;
            unsigned uSecsToDelay = (unsigned)(scs.duration*1000000);
            scs.streamTimerTask = env.taskScheduler().scheduleDelayedTask(uSecsToDelay, (TaskFunc*)streamTimerHandler, rtspClient);
        }

//        env << "Started playing session";
        if (scs.duration > 0) {
            env << " (for up to " << scs.duration << " seconds)";
        }
//        env << "...\n";

        success = True;
    } while (0);
    delete[] resultString;

    if (!success) {

        shutdownStream(rtspClient);
    }
}

void subsessionAfterPlaying(void* clientData) {
    MediaSubsession* subsession = (MediaSubsession*)clientData;
    RTSPClient* rtspClient = (RTSPClient*)(subsession->miscPtr);

    Medium::close(subsession->sink);
    subsession->sink = NULL;

    MediaSession& session = subsession->parentSession();
    MediaSubsessionIterator iter(session);
    while ((subsession = iter.next()) != NULL) {
        if (subsession->sink != NULL) return;
    }

    shutdownStream(rtspClient);
}

void subsessionByeHandler(void* clientData) {
    MediaSubsession* subsession = (MediaSubsession*)clientData;
    RTSPClient* rtspClient = (RTSPClient*)subsession->miscPtr;
    UsageEnvironment& env = rtspClient->envir();

#ifdef DDEBUG
    env << getTimeFormat() << "Received RTCP \"BYE\" on subsession\n\n";
#endif

    subsessionAfterPlaying(subsession);
}

void streamTimerHandler(void* clientData) {
    VideoClient* rtspClient = (VideoClient*)clientData;
    UsageEnvironment& env = rtspClient->envir();
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
    env.taskScheduler().unscheduleDelayedTask(scs.streamTimerTask);
    scs.streamTimerTask = NULL;

    shutdownStream(rtspClient);
}

void timeoutHandler(void *clientData){
    VideoClient* rtspClient = (VideoClient*)clientData;
    UsageEnvironment& env = rtspClient->envir();
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
    env.taskScheduler().unscheduleDelayedTask(scs.timeoutTask);
    scs.timeoutTask = NULL;
#ifdef DDEBUG
    printf("timeoutHandler");
#endif
    if (rtspClient != NULL && rtspClient->frameHandler != NULL) {
        rtspClient->frameHandler(rtspClient,ConnectFail,VideoTypeH264, NULL,0, false);
    }
}

void needRebootHandler(void *clientData){
    VideoClient* rtspClient = (VideoClient*)clientData;
    UsageEnvironment& env = rtspClient->envir();
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
    env.taskScheduler().unscheduleDelayedTask(scs.timeoutTask);
    scs.timeoutTask = NULL;
#ifdef DDEBUG
    printf("needRebootHandler");
#endif

    if (rtspClient != NULL && rtspClient->frameHandler != NULL) {
        rtspClient->frameHandler(rtspClient,ConnectServerHasShutdown,VideoTypeH264, NULL,0, false);
    }
}

void sendDescribFunction(void *clientData){
    VideoClient* rtspClient = (VideoClient*)clientData;
    UsageEnvironment& env = rtspClient->envir();
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs;
    env.taskScheduler().unscheduleDelayedTask(scs.timeoutTask);
    scs.timeoutTask = NULL;
#ifdef DDEBUG
    printf("sendDescribFunction");
#endif
    rtspClient->sendDescribeCommand(continueAfterDESCRIBE);
}

void shutdownStream(RTSPClient* rtspClient, int exitCode) {

    UsageEnvironment& env = rtspClient->envir(); // alias
    StreamClientState& scs = ((VideoClient*)rtspClient)->scs; // alias

    // First, check whether any subsessions have still to be closed:
    if (scs.session != NULL) {
        Boolean someSubsessionsWereActive = False;
        MediaSubsessionIterator iter(*scs.session);
        MediaSubsession* subsession;

        while ((subsession = iter.next()) != NULL) {
            if (subsession->sink != NULL) {
                Medium::close(subsession->sink);
                subsession->sink = NULL;

                if (subsession->rtcpInstance() != NULL) {
                    subsession->rtcpInstance()->setByeHandler(NULL, NULL); // in case the server sends a RTCP "BYE" while handling "TEARDOWN"
                }

                someSubsessionsWereActive = True;
            }
        }

        if (someSubsessionsWereActive) {
            // Send a RTSP "TEARDOWN" command, to tell the server to shutdown the stream.
            // Don't bother handling the response to the "TEARDOWN".
            rtspClient->sendTeardownCommand(*scs.session, NULL);
        }
    }

//    env << *rtspClient << "Closing the stream.\n";
    Medium::close(rtspClient);
}
