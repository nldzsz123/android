//
// Created by myapplication on 16/9/3.
//
#include <jni.h>
#include <pthread.h>
#include "CPPLog.h"
#include "VideoClient.h"
#include "Decoder/VideoProcessExtractor.h"
#include "MuxerToMP4.hpp"
#include <mediametadataretriever.h>
#include <ffmpegExtractor/CommonDefine.h>

extern "C" {
#include "libavcodec/jni.h"
#include "libswscale/swscale.h"
#include "tcp_client.h"
}

typedef struct {
    JavaVM *javaVM;
    jobject javaCallbackInstance;
    jmethodID updateCallback;
    jmethodID connectStatusCallback;
    ANativeWindow *window;

    char eventLoopWatchVariable = 0;
    bool videoInit;
    bool enableHardDecode;
    const char *playerUrl;

    VideoProcessExtractor *process;

    pthread_mutex_t tMutex;
} JavaVMContext;
JavaVMContext g_ctx;

char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

// 声明函数
void decodeComplete(void *client, VideoFrameYUV *yuvFrame);

bool getJNIEnv(JNIEnv **env) {
    JavaVM *javaVM = g_ctx.javaVM;
    jint res = javaVM->GetEnv((void **) env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = javaVM->AttachCurrentThread(env, NULL);
        if (JNI_OK != res) {
            LOGD("Failed to AttachCurrentThread, ErrorCode = %d", res);
            return false;
        }
    }

    return true;
}

// 由调用者自己释放返回的字符串的内存
char *jstringTochar(JNIEnv *env, jstring js) {
    int len = env->GetStringLength(js);
    char *js_ = (char *) malloc(len + 1);
    const char *src = env->GetStringUTFChars(js, 0);
    strcpy(js_, src);
    js_[len + 1] = '\0';

    env->ReleaseStringUTFChars(js, src);

    return js_;
}

jboolean init(JNIEnv *env, jobject instance, jstring url_, jboolean enableHardDecode) {
    g_ctx.videoInit = false;
    g_ctx.enableHardDecode = enableHardDecode;

    //初始化解码相关环境
    g_ctx.process = new VideoProcessExtractor(enableHardDecode);
    g_ctx.process->enableHardDecode(enableHardDecode);
    g_ctx.process->setDecodeCallback(NULL, decodeComplete);

    g_ctx.javaCallbackInstance = env->NewGlobalRef(instance);
    jclass clazz = env->FindClass("com/videoplayer/VideoClient");
//    jmethodID update_id1 = env->GetMethodID(env->GetObjectClass(instance), "update", "([B[B[BII)V");
    jmethodID update_id1 = env->GetMethodID(clazz, "update", "([B[B[BII)V");
    if (update_id1 == NULL) {
        LOGD("回调方法不存在");
        return false;
    }
    g_ctx.updateCallback = update_id1;

    jmethodID update_id = env->GetMethodID(clazz, "connectStatus", "(III)V");
    if (update_id == NULL) {
        LOGD("回调方法不存在");
        return false;
    }
    g_ctx.connectStatusCallback = update_id;

    pthread_mutex_lock(&g_ctx.tMutex);
    if (g_ctx.playerUrl != NULL) {
        free((void *) g_ctx.playerUrl);
    }
    g_ctx.playerUrl = jstringTochar(env, url_);
    pthread_mutex_unlock(&g_ctx.tMutex);

    LOGD("初始化成功 %s", g_ctx.playerUrl);

    return true;
}

void receiveVideoFrame(void *client, ConnectStatus status, VideoType type, uint8_t *video, int size,
                       bool continueSeq) {
    JNIEnv *env;
    if (!getJNIEnv(&env)) {
        return;
    }

    if (g_ctx.eventLoopWatchVariable == 1) {
        return;
    }

    if (status != ConnectSucess) {
        // 连接出错
        env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.connectStatusCallback, 1, 0,0);
        return;
    }

    size_t videoSize = (size_t) size;
    // 为视频数据加上头0001
    uint8_t *bufferWithStartCode = (uint8_t *) malloc(videoSize + 4);
    memset(bufferWithStartCode, 0, videoSize + 4);
    bufferWithStartCode[0] = 0;
    bufferWithStartCode[1] = 0;
    bufferWithStartCode[2] = 0;
    bufferWithStartCode[3] = 1;
    memcpy(bufferWithStartCode + 4, video, videoSize);

    // 开始解析
    VideoProcessExtractor *process = g_ctx.process;
    process->pushOrignalVideoData(bufferWithStartCode, size + 4, type, continueSeq);

    // 解析完毕
    if (bufferWithStartCode) {
        free(bufferWithStartCode);
        bufferWithStartCode = NULL;
    }
}

void decodeComplete(void *client, VideoFrameYUV *yuvFrame) {
    if (yuvFrame == NULL) {
//        LOGD("yuvFrame is NULL");
        return;
    }

    JNIEnv *env = NULL;
    if (!getJNIEnv(&env)) {
        LOGD("decodeComplete getJNIEnv fail");
        return;
    }

    int videoHeight = yuvFrame->height;
    int videoWidth = yuvFrame->width;
    int sizeY = videoWidth * videoHeight;
    int sizeU = videoWidth * videoHeight * 0.25;
    int sizeV = videoWidth * videoHeight * 0.25;
    uint8_t *bufY = yuvFrame->luma;
    uint8_t *bufU = yuvFrame->chromaB;
    uint8_t *bufV = yuvFrame->chromaR;


    jbyteArray yBytes = env->NewByteArray(sizeY);
    env->SetByteArrayRegion(yBytes, 0, sizeY, (jbyte *) bufY);

    jbyteArray uBytes = env->NewByteArray(sizeU);
    env->SetByteArrayRegion(uBytes, 0, sizeU, (jbyte *) bufU);

    jbyteArray vBytes = env->NewByteArray(sizeV);
    env->SetByteArrayRegion(vBytes, 0, sizeV, (jbyte *) bufV);

    // 回调连接状态
    if (!g_ctx.videoInit && g_ctx.eventLoopWatchVariable == 0) {
        env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.connectStatusCallback, 0, videoWidth,
                            videoHeight);
        g_ctx.videoInit = true;
    }
    // 回调解码成功的数据；用于渲染
    env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.updateCallback, yBytes, uBytes, vBytes,
                        videoWidth, videoHeight);
    env->DeleteLocalRef(yBytes);
    env->DeleteLocalRef(uBytes);
    env->DeleteLocalRef(vBytes);

}


void initJavaVMContext(JavaVM *vm) {
    av_jni_set_java_vm(vm, NULL);

    memset(&g_ctx, 0, sizeof(g_ctx));
    g_ctx.javaVM = vm;
    g_ctx.javaCallbackInstance = NULL;
    g_ctx.updateCallback = NULL;
    g_ctx.connectStatusCallback = NULL;
    g_ctx.window = NULL;
    g_ctx.videoInit = false;
    g_ctx.enableHardDecode = false;
    g_ctx.process = NULL;
    g_ctx.playerUrl = NULL;
    g_ctx.tMutex = PTHREAD_MUTEX_INITIALIZER;
}

extern "C" void
Java_com_videoplayer_NativeCode_setSurface(JNIEnv *env, jclass instance, jobject surface) {
    // obtain a native window from a Java surface
    if (g_ctx.window) {
        ANativeWindow_release(g_ctx.window);
        g_ctx.window = NULL;
    }
    g_ctx.window = ANativeWindow_fromSurface(env, surface);
    if (g_ctx.enableHardDecode && g_ctx.window != NULL) {
        g_ctx.process->setRenderWindow(g_ctx.window);
    }
    LOGD("@@@ setsurface %p", g_ctx.window);
}

extern "C" jboolean
Java_com_videoplayer_NativeCode_init(JNIEnv *env, jclass instance, jobject callbackObj, jstring url,
                                     jboolean enableHardDecode) {
    return init(env, callbackObj, url, enableHardDecode);
}

extern "C" void Java_com_videoplayer_NativeCode_play(JNIEnv *, jclass) {
    TaskScheduler *scheduler = BasicTaskScheduler::createNew();
    UsageEnvironment *env = BasicUsageEnvironment::createNew(*scheduler);

    pthread_mutex_lock(&g_ctx.tMutex);
    const char *url = g_ctx.playerUrl;
    pthread_mutex_unlock(&g_ctx.tMutex);

    VideoClient *rtspClient = new VideoClient(*env, url);
    rtspClient->frameHandler = receiveVideoFrame;
    rtspClient->startStream();
    g_ctx.eventLoopWatchVariable = 0;
    env->taskScheduler().doEventLoop(&g_ctx.eventLoopWatchVariable);

    rtspClient->shuttStream();
    env->reclaim(); env = NULL;
    delete scheduler; scheduler = NULL;
    LOGD("videoplayer video strem will close");
}

extern "C" void Java_com_videoplayer_NativeCode_stop(JNIEnv *, jclass) {
    g_ctx.eventLoopWatchVariable = 1;
    g_ctx.videoInit = false;
}

extern "C" void Java_com_videoplayer_NativeCode_weakeup(JNIEnv *, jclass) {
    g_ctx.process->weakeupa();
}

extern "C" void Java_com_videoplayer_NativeCode_decode(JNIEnv *, jclass) {
    // 开始解码
    VideoProcessExtractor *process = g_ctx.process;
    // 硬解码直接渲染在surface上
    VideoFrameYUV *yuvframe = process->decode();
    // 软解码才需要回调
    if (!g_ctx.enableHardDecode) {
        decodeComplete(NULL, yuvframe);
    }
}
extern "C" void Java_com_videoplayer_NativeCode_weekup(JNIEnv *, jclass) {
    if (g_ctx.process != NULL) {
        g_ctx.process->weakeupa();
        g_ctx.process->clearVideoData();
    }
}

extern "C" void Java_com_videoplayer_NativeCode_releaseResources(JNIEnv *, jclass) {
    if (g_ctx.process != NULL) {
        g_ctx.process->clearVideoData();
        delete g_ctx.process;
        g_ctx.process = NULL;
    }

    g_ctx.javaCallbackInstance = NULL;
    g_ctx.updateCallback = NULL;
    g_ctx.connectStatusCallback = NULL;
    g_ctx.videoInit = false;
    g_ctx.enableHardDecode = false;
    g_ctx.playerUrl = NULL;
    pthread_mutex_destroy(&g_ctx.tMutex);
    if (g_ctx.window) {
        ANativeWindow_release(g_ctx.window);
        g_ctx.window = NULL;
    }

}

AVFrame *m_pAVFrame;
AVCodec *m_pAVCodec;
AVCodecContext *m_pAVCodecContext;
pthread_mutex_t mutex_lock = PTHREAD_MUTEX_INITIALIZER;
extern "C" jbyteArray
Java_com_videoplayer_NativeCode_nativeFillBitmap(JNIEnv *env, jclass jclas, jbyteArray bytes,
                                                 jfloat atio) {
    pthread_mutex_lock(&mutex_lock);
    if (m_pAVFrame == NULL) {
        m_pAVFrame = av_frame_alloc();
    }

    if (m_pAVCodec == NULL) {
        avcodec_register_all();
        m_pAVCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
        if (!m_pAVCodec) {
            LOGD("ffmpeg error, can not find decoder");
            pthread_mutex_unlock(&mutex_lock);
            return NULL;
        }
    }

    if (m_pAVCodecContext == NULL) {
        m_pAVCodecContext = avcodec_alloc_context3(m_pAVCodec);
        if (avcodec_open2(m_pAVCodecContext, m_pAVCodec, NULL) < 0) {
            LOGD("ffmpeg error, can not open decoder");
            pthread_mutex_unlock(&mutex_lock);
            return NULL;
        }
    }

    int bufferSize = env->GetArrayLength(bytes);
    uint8_t *videoBuffer = (uint8_t *) malloc(bufferSize);
    env->GetByteArrayRegion(bytes, 0, bufferSize, reinterpret_cast<jbyte *>(videoBuffer));
    AVPacket avpkt;
    av_init_packet(&avpkt);
    avpkt.size = bufferSize;
    avpkt.data = videoBuffer;
    bool isEAGAIN = false;
    bool exitWhile = true;
    do {
        if (isEAGAIN) {
            avcodec_send_packet(m_pAVCodecContext, NULL);
        } else {
            if (avcodec_send_packet(m_pAVCodecContext, &avpkt) != 0) {
                LOGD("avcodec_send_packet fail");
                pthread_mutex_unlock(&mutex_lock);
                return NULL;
            }
        }
        int ret = avcodec_receive_frame(m_pAVCodecContext, m_pAVFrame);
        if (ret == AVERROR(EAGAIN)) {
            LOGD("avcodec_receive_frame fail %d", ret);
            isEAGAIN = true;
            exitWhile = false;
        } else if (ret < 0) {
            pthread_mutex_unlock(&mutex_lock);
            return NULL;
        } else {
            exitWhile = true;
        }

    } while (!exitWhile);

    int videoHeight = m_pAVFrame->height * atio;
    int videoWidth = m_pAVFrame->width * atio;

    jclass clazz = env->FindClass("com/videoplayer/VideoClient");
    if (clazz == NULL) {
        LOGD("类不存在");
        pthread_mutex_unlock(&mutex_lock);
        return NULL;
    }

    jmethodID setwh = env->GetStaticMethodID(clazz, "setWH", "(II)V");
    if (setwh == NULL) {
        printf("找不到setWH这个静态方法。");
        pthread_mutex_unlock(&mutex_lock);
        return NULL;
    }
    env->CallStaticVoidMethod(clazz, setwh, videoWidth, videoHeight);
    AVPicture picture;
    avpicture_alloc(&picture, AV_PIX_FMT_RGB24, videoWidth, videoHeight);
    static int sws_flags = SWS_FAST_BILINEAR;
    struct SwsContext *img_convert_ctx;
    img_convert_ctx = sws_getContext(m_pAVCodecContext->width,
                                     m_pAVCodecContext->height,
                                     m_pAVCodecContext->pix_fmt,
                                     videoWidth,
                                     videoHeight,
                                     AV_PIX_FMT_RGB24,
                                     sws_flags, NULL, NULL, NULL);
    sws_scale(img_convert_ctx,
              (const uint8_t *const *) m_pAVFrame->data,
              m_pAVFrame->linesize,
              0,
              m_pAVCodecContext->height,
              picture.data,
              picture.linesize);

    int size = picture.linesize[0] * videoHeight;
    uint8_t *data = picture.data[0];
    jbyteArray array = env->NewByteArray(size);;
    jbyte *bbytes = env->GetByteArrayElements(array, NULL);
    if (bbytes != NULL) {
        memcpy(bbytes, data, size);
        env->ReleaseByteArrayElements(array, bbytes, 0);
    }

    av_free_packet(&avpkt);
    avpicture_free(&picture);
    av_free_packet(&avpkt);
    sws_freeContext(img_convert_ctx);
    free(videoBuffer);
    avcodec_flush_buffers(m_pAVCodecContext);

    pthread_mutex_unlock(&mutex_lock);
    return array;

}



// ======设置遥控器相关=======
extern "C" jboolean
Java_com_videoplayer_NativeCode_initSocket(JNIEnv *env, jclass jclas, jstring ip, jint port,
                                           jint timeout) {
    const char *_ip = jstringToChar(env, ip);
    initSocket(_ip, port);
    return connectToServer(timeout) == 0;
}
extern "C" jint Java_com_videoplayer_NativeCode_setIp(JNIEnv *env, jclass jclas, jstring url) {

    char *_url = jstringToChar(env, url);
    int len = env->GetStringLength(url);
    LOGD("_url %s", _url);
    return setIp(_url, len);
}

extern "C" jboolean
Java_com_videoplayer_NativeCode_duiping(JNIEnv *env, jclass jclas, jstring ssid) {

    char *_ssid = (char *) env->GetStringUTFChars(ssid, 0);
    int len = env->GetStringLength(ssid);
    env->ReleaseStringUTFChars(ssid, _ssid);
    LOGD("_ssid %s", _ssid);
    return duiping(_ssid, len) == 0;
}

extern "C" jstring Java_com_videoplayer_NativeCode_getRSSI(JNIEnv *env, jclass jclas) {
    char *str = getRSSI();
    LOGE("str %s", str);
    if (str == NULL) {
        return NULL;
    }
    jstring result = env->NewStringUTF(str);
    free(str);
    str = NULL;
    return result;
}

extern "C" void Java_com_videoplayer_NativeCode_coloseSocket(JNIEnv *env, jobject instance) {
//    closeSocket();
}
// ========= 设置遥控器相关 ============= ////



// =============== 提取视频相关 ==========================
struct fields_t {
    jfieldID context;
};

static fields_t fields;
static const char *const kClassPathName = "wseemann/media/FFmpegMediaMetadataRetriever";

// video sink for the player
static ANativeWindow *theNativeWindow;

static jstring NewStringUTF(JNIEnv *env, const char *data) {
    jstring str = NULL;

    int size = strlen(data);

    jbyteArray array = NULL;
    array = env->NewByteArray(size);
    if (!array) {  // OutOfMemoryError exception has already been thrown.
//        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
//                            "convertString: OutOfMemoryError is thrown.");
    } else {
        jbyte *bytes = env->GetByteArrayElements(array, NULL);
        if (bytes != NULL) {
            memcpy(bytes, data, size);
            env->ReleaseByteArrayElements(array, bytes, 0);

            jclass string_Clazz = env->FindClass("java/lang/String");
            jmethodID string_initMethodID = env->GetMethodID(string_Clazz, "<init>",
                                                             "([BLjava/lang/String;)V");
            jstring utf = env->NewStringUTF("UTF-8");
            str = (jstring) env->NewObject(string_Clazz, string_initMethodID, array, utf);

            env->DeleteLocalRef(utf);
            //env->DeleteLocalRef(str);
        }
    }
    env->DeleteLocalRef(array);


    return str;
}

void jniThrowException(JNIEnv *env, const char *className,
                       const char *msg) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, msg);
}

static void process_media_retriever_call(JNIEnv *env, int opStatus, const char *exception,
                                         const char *message) {
    if (opStatus == -2) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
    } else if (opStatus == -1) {
        if (strlen(message) > 230) {
            // If the message is too long, don't bother displaying the status code.
            jniThrowException(env, exception, message);
        } else {
            char msg[256];
            // Append the status code to the message.
            sprintf(msg, "%s: status = 0x%X", message, opStatus);
            jniThrowException(env, exception, msg);
        }
    }
}

static MediaMetadataRetriever *getRetriever(JNIEnv *env, jobject thiz) {
    // No lock is needed, since it is called internally by other methods that are protected
    MediaMetadataRetriever *retriever = (MediaMetadataRetriever *) env->GetLongField(thiz,
                                                                                     fields.context);
    return retriever;
}

static void setRetriever(JNIEnv *env, jobject thiz, long retriever) {
    // No lock is needed, since it is called internally by other methods that are protected
    MediaMetadataRetriever *old = (MediaMetadataRetriever *) env->GetLongField(thiz,
                                                                               fields.context);
    env->SetLongField(thiz, fields.context, retriever);
}

static void
wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceAndHeaders(
        JNIEnv *env, jobject thiz, jstring path,
        jobjectArray keys, jobjectArray values) {

    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return;
    }

    if (!path) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "Null pointer");
        return;
    }

    const char *tmp = env->GetStringUTFChars(path, NULL);
    if (!tmp) {  // OutOfMemoryError exception already thrown
        return;
    }

    // Don't let somebody trick us in to reading some random block of memory
    if (strncmp("mem://", tmp, 6) == 0) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "Invalid pathname");
        return;
    }

    // Workaround for FFmpeg ticket #998
    // "must convert mms://... streams to mmsh://... for FFmpeg to work"
    char *restrict_to = (char *) strstr(tmp, "mms://");
    if (restrict_to) {
        strncpy(restrict_to, "mmsh://", 6);
        puts(tmp);
    }

    char *headers = NULL;

    if (keys && values != NULL) {
        int keysCount = env->GetArrayLength(keys);
        int valuesCount = env->GetArrayLength(values);

        if (keysCount != valuesCount) {
            jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
            return;
        }

        int i = 0;
        const char *rawString = NULL;
        char hdrs[2048];

        for (i = 0; i < keysCount; i++) {
            jstring key = (jstring) env->GetObjectArrayElement(keys, i);
            rawString = env->GetStringUTFChars(key, NULL);
            strcat(hdrs, rawString);
            strcat(hdrs, ": ");
            env->ReleaseStringUTFChars(key, rawString);

            jstring value = (jstring) env->GetObjectArrayElement(values, i);
            rawString = env->GetStringUTFChars(value, NULL);
            strcat(hdrs, rawString);
            strcat(hdrs, "\r\n");
            env->ReleaseStringUTFChars(value, rawString);
        }

        headers = &hdrs[0];
    }

    process_media_retriever_call(
            env,
            retriever->setDataSource(tmp, headers),
            "java/lang/IllegalArgumentException",
            "setDataSource failed");

    env->ReleaseStringUTFChars(path, tmp);
    tmp = NULL;
}

static void wseemann_media_FFmpegMediaMetadataRetriever_setDataSource(
        JNIEnv *env, jobject thiz, jstring path) {
    wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceAndHeaders(
            env, thiz, path, NULL, NULL);
}

static int jniGetFDFromFileDescriptor(JNIEnv *env, jobject fileDescriptor) {
    jint fd = -1;
    jclass fdClass = env->FindClass("java/io/FileDescriptor");

    if (fdClass != NULL) {
        jfieldID fdClassDescriptorFieldID = env->GetFieldID(fdClass, "descriptor", "I");
        if (fdClassDescriptorFieldID != NULL && fileDescriptor != NULL) {
            fd = env->GetIntField(fileDescriptor, fdClassDescriptorFieldID);
        }
    }

    return fd;
}

static void wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceFD(JNIEnv *env, jobject thiz,
                                                                        jobject fileDescriptor,
                                                                        jlong offset,
                                                                        jlong length) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return;
    }
    if (!fileDescriptor) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return;
    }
    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (offset < 0 || length < 0 || fd < 0) {
        if (offset < 0) {
//            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "negative offset (%lld)", offset);
        }
        if (length < 0) {
//            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "negative length (%lld)", length);
        }
        if (fd < 0) {
//            __android_log_write(ANDROID_LOG_ERROR, LOG_TAG, "invalid file descriptor");
        }
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return;
    }
    process_media_retriever_call(env, retriever->setDataSource(fd, offset, length),
                                 "java/lang/RuntimeException", "setDataSource failed");
}

static jbyteArray wseemann_media_FFmpegMediaMetadataRetriever_getFrameAtTime(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong timeUs,
                                                                             jint option) {
    //__android_log_write(ANDROID_LOG_INFO, LOG_TAG, "getFrameAtTime");
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return NULL;
    }

    AVPacket packet;
    av_init_packet(&packet);
    jbyteArray array = NULL;

    if (retriever->getFrameAtTime(timeUs, option, &packet) == 0) {
        int size = packet.size;
        uint8_t *data = packet.data;
        array = env->NewByteArray(size);
        if (!array) {  // OutOfMemoryError exception has already been thrown.

        } else {
            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
    }

    av_packet_unref(&packet);

    return array;
}

static jbyteArray wseemann_media_FFmpegMediaMetadataRetriever_getScaledFrameAtTime(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jlong timeUs,
                                                                                   jint option,
                                                                                   jint width,
                                                                                   jint height) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return NULL;
    }

    AVPacket packet;
    av_init_packet(&packet);
    jbyteArray array = NULL;

    if (retriever->getScaledFrameAtTime(timeUs, option, &packet, width, height) == 0) {
        int size = packet.size;
        uint8_t *data = packet.data;
        array = env->NewByteArray(size);
        if (!array) {  // OutOfMemoryError exception has already been thrown.

        } else {
            //__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "getFrameAtTime: Got frame.");
            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
    }

    av_packet_unref(&packet);

    return array;
}

static jbyteArray wseemann_media_FFmpegMediaMetadataRetriever_getEmbeddedPicture(JNIEnv *env,
                                                                                 jobject thiz) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return NULL;
    }

    AVPacket packet;
    av_init_packet(&packet);
    jbyteArray array = NULL;

    if (retriever->extractAlbumArt(&packet) == 0) {
        int size = packet.size;
        uint8_t *data = packet.data;
        array = env->NewByteArray(size);
        if (!array) {  // OutOfMemoryError exception has already been thrown.

        } else {

            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
    }

    av_packet_unref(&packet);

    return array;
}

static jobject wseemann_media_FFmpegMediaMetadataRetriever_extractMetadata(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jstring jkey) {

    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return NULL;
    }

    if (!jkey) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "Null pointer");
        return NULL;
    }

    const char *key = env->GetStringUTFChars(jkey, NULL);
    if (!key) {  // OutOfMemoryError exception already thrown
        return NULL;
    }

    const char *value = retriever->extractMetadata(key);
    if (!value) {
        return NULL;
    }

    env->ReleaseStringUTFChars(jkey, key);
    return NewStringUTF(env, value);
}

static jobject wseemann_media_FFmpegMediaMetadataRetriever_extractMetadataFromChapter(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring jkey,
                                                                                      jint chapter) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return NULL;
    }

    if (!jkey) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "Null pointer");
        return NULL;
    }

    const char *key = env->GetStringUTFChars(jkey, NULL);
    if (!key) {  // OutOfMemoryError exception already thrown
        return NULL;
    }

    if (chapter < 0) {
        return NULL;
    }

    const char *value = retriever->extractMetadataFromChapter(key, chapter);
    if (!value) {
        return NULL;
    }
    //__android_log_print(ANDROID_LOG_INFO, LOG_TAG, "extractMetadata: value (%s) for keyCode(%s)", value, key);
    env->ReleaseStringUTFChars(jkey, key);
    return env->NewStringUTF(value);
}

static jobject
wseemann_media_FFmpegMediaMetadataRetriever_getMetadata(JNIEnv *env, jobject thiz,
                                                        jboolean update_only,
                                                        jboolean apply_filter, jobject reply) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return JNI_FALSE;
    }

    // On return metadata is positioned at the beginning of the
    // metadata. Note however that the parcel actually starts with the
    // return code so you should not rewind the parcel using
    // setDataPosition(0).
    AVDictionary *metadata = NULL;

    if (retriever->getMetadata(update_only, apply_filter, &metadata) == 0) {
        jclass hashMap_Clazz = env->FindClass("java/util/HashMap");
        jmethodID gHashMap_initMethodID = env->GetMethodID(hashMap_Clazz, "<init>", "()V");
        jobject map = env->NewObject(hashMap_Clazz, gHashMap_initMethodID);
        jmethodID gHashMap_putMethodID = env->GetMethodID(hashMap_Clazz, "put",
                                                          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        int i = 0;

        for (i = 0; i < metadata->count; i++) {
            jstring jKey = NewStringUTF(env, metadata->elems[i].key);
            jstring jValue = NewStringUTF(env, metadata->elems[i].value);
            (jobject) env->CallObjectMethod(map, gHashMap_putMethodID, jKey, jValue);
            env->DeleteLocalRef(jKey);
            env->DeleteLocalRef(jValue);
        }

        if (metadata) {
            av_dict_free(&metadata);

        }

        return map;
    } else {
        return reply;
    }
}

static void wseemann_media_FFmpegMediaMetadataRetriever_release(JNIEnv *env, jobject thiz) {
    //Mutex::Autolock lock(sLock);
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    delete retriever;
    setRetriever(env, thiz, 0);
}

// set the surface
static void wseemann_media_FFmpegMediaMetadataRetriever_setSurface(JNIEnv *env, jclass thiz,
                                                                   jobject surface) {
    MediaMetadataRetriever *retriever = getRetriever(env, thiz);
    if (retriever == 0) {
        jniThrowException(env, "java/lang/IllegalStateException", "No retriever available");
        return;
    }

    // obtain a native window from a Java surface
    theNativeWindow = ANativeWindow_fromSurface(env, surface);

    if (theNativeWindow != NULL) {
        retriever->setNativeWindow(theNativeWindow);
    }
}

static void wseemann_media_FFmpegMediaMetadataRetriever_native_finalize(JNIEnv *env, jobject thiz) {
    // No lock is needed, since Java_wseemann_media_FFmpegMediaMetadataRetriever_release() is protected
    wseemann_media_FFmpegMediaMetadataRetriever_release(env, thiz);
}

static void wseemann_media_FFmpegMediaMetadataRetriever_native_init(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(kClassPathName);
    if (clazz == NULL) {
        return;
    }

    fields.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (fields.context == NULL) {
        return;
    }


    // Initialize libavformat and register all the muxers, demuxers and protocols.
    av_register_all();
    avformat_network_init();
}

static void wseemann_media_FFmpegMediaMetadataRetriever_native_setup(JNIEnv *env, jobject thiz) {
    MediaMetadataRetriever *retriever = new MediaMetadataRetriever();
    if (retriever == 0) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    setRetriever(env, thiz, (long) retriever);
}

// JNI mapping between Java methods and native methods
static JNINativeMethod nativeMethods[] = {
        {"setDataSource",              "(Ljava/lang/String;)V",                      (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSource},

        {
         "_setDataSource",
                                       "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V",
                                                                                     (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceAndHeaders
        },

        {"setDataSource",              "(Ljava/io/FileDescriptor;JJ)V",              (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceFD},
        {"_getFrameAtTime",            "(JI)[B",                                     (void *) wseemann_media_FFmpegMediaMetadataRetriever_getFrameAtTime},
        {"_getScaledFrameAtTime",      "(JIII)[B",                                   (void *) wseemann_media_FFmpegMediaMetadataRetriever_getScaledFrameAtTime},
        {"extractMetadata",            "(Ljava/lang/String;)Ljava/lang/String;",     (void *) wseemann_media_FFmpegMediaMetadataRetriever_extractMetadata},
        {"extractMetadataFromChapter", "(Ljava/lang/String;I)Ljava/lang/String;",    (void *) wseemann_media_FFmpegMediaMetadataRetriever_extractMetadataFromChapter},
        {"native_getMetadata",         "(ZZLjava/util/HashMap;)Ljava/util/HashMap;", (void *) wseemann_media_FFmpegMediaMetadataRetriever_getMetadata},
        {"getEmbeddedPicture",         "()[B",                                       (void *) wseemann_media_FFmpegMediaMetadataRetriever_getEmbeddedPicture},
        {"release",                    "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_release},
        {"setSurface",                 "(Ljava/lang/Object;)V",                      (void *) wseemann_media_FFmpegMediaMetadataRetriever_setSurface},
        {"native_finalize",            "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_finalize},
        {"native_setup",               "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_setup},
        {"native_init",                "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_init},
};

// This function only registers the native methods, and is called from
// JNI_OnLoad in wseemann_media_FFmpegMediaMetadataRetriever.cpp
int register_wseemann_media_FFmpegMediaMetadataRetriever(JNIEnv *env) {
    int numMethods = (sizeof(nativeMethods) / sizeof((nativeMethods)[0]));
    jclass clazz = env->FindClass("wseemann/media/FFmpegMediaMetadataRetriever");
    jint ret = env->RegisterNatives(clazz, nativeMethods, numMethods);
    env->DeleteLocalRef(clazz);
    return ret;
}

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGD("JNI version not supported.");
        return JNI_ERR;
    }

    if (register_wseemann_media_FFmpegMediaMetadataRetriever(env) < 0) {
        LOGD("ERROR: FFmpegMediaMetadataRetriever native registration failed");
        return JNI_ERR;
    }

    initJavaVMContext(vm);

    /* success -- return valid version number */
    return JNI_VERSION_1_6;
}

extern "C" void
Java_com_videoplayer_NativeCode_aviToMp4(JNIEnv *env, jclass, jstring inputPath_,
                                         jstring outputPath_) {
    size_t inputUrlLen = env->GetStringLength(inputPath_);
    char *inputUrl = (char *) malloc(inputUrlLen + 1);
    env->GetStringUTFRegion(inputPath_, 0, inputUrlLen, inputUrl);
    inputUrl[inputUrlLen] = '\0';

    size_t outUrlLen = env->GetStringLength(outputPath_);
    char *outUrl = (char *) malloc(outUrlLen + 1);
    env->GetStringUTFRegion(outputPath_, 0, outUrlLen, outUrl);
    outUrl[outUrlLen] = '\0';
    MuxerToMP4 muxerToMp4(inputUrl, outUrl);
    muxerToMp4.aviToMp4();

}