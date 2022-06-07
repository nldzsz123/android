#include <jni.h>
#include <string>
#include <iostream>
#include "LogUtils.h"
#include <alloca.h>
#include "CVWrapper.h"

// 用于遥控器对频
#include  <sys/types.h>
#include  <sys/socket.h>
#include  <stdio.h>
#include  <netinet/in.h>
#include  <arpa/inet.h>
#include  <unistd.h>
#include  <string.h>
#include  <netdb.h>
#include  <sys/ioctl.h>
#include  <termios.h>
#include  <stdlib.h>
#include  <sys/stat.h>
#include  <fcntl.h>
#include  <signal.h>
#include  <sys/time.h>

#define SSIDLEN 6 // wifi除了FP开头的长度

using namespace std;
typedef struct tick_context {
    JavaVM *javaVM;
    jclass javaCallbackClass;
    jobject javaCallbackInstance;
    jmethodID mainMethodId1;
    jmethodID mainMethodId2;
} TickContext;
TickContext g_ctx;

// 监听全景合成进度的回调
void receiveProgressAfter(int progress);

void resultAfter(int err);

extern "C" JNIEXPORT jint

JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    memset(&g_ctx, 0, sizeof(g_ctx));

    g_ctx.javaVM = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGD("JNI version not supported.");
        return JNI_ERR;
    }
    g_ctx.javaCallbackClass = NULL;
    g_ctx.javaCallbackInstance = NULL;

    return JNI_VERSION_1_6;
}


extern "C" void Java_opencv_OpencvLib_regulationPanoramaToFile(JNIEnv *env, jobject instance,
                                                               jstring panorPath_,
                                                               jint saturation, jint contrast,
                                                               jint brightness,
                                                               jstring savePath_) {

    size_t panorPathLength = env->GetStringLength(panorPath_);
    char *panorPath = (char *) malloc(panorPathLength + 1);
    env->GetStringUTFRegion(panorPath_, 0, panorPathLength, panorPath);
    panorPath[panorPathLength] = '\0';

    size_t savePathLength = env->GetStringLength(savePath_);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(savePath_, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';
    LOGE("保存路径 = %d", savePath);

    CVWrapper wrapper = CVWrapper();
    wrapper.editPano(panorPath, saturation, contrast, brightness, savePath);
}

extern "C" void Java_opencv_OpencvLib_fanzhuan(JNIEnv *env, jobject instance,
                                               jstring panorPath_,
                                               jstring savePath_) {

    size_t panorPathLength = env->GetStringLength(panorPath_);
    char *panorPath = (char *) malloc(panorPathLength + 1);
    env->GetStringUTFRegion(panorPath_, 0, panorPathLength, panorPath);
    panorPath[panorPathLength] = '\0';

    size_t savePathLength = env->GetStringLength(savePath_);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(savePath_, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';
    LOGE("保存路径 = %d", savePath);

    CVWrapper wrapper = CVWrapper();
    wrapper.fanzhuan(panorPath, savePath);
}

extern "C" jintArray Java_opencv_OpencvLib_regulationPanoramaToMemory(JNIEnv *env, jobject instance,
                                                                      jintArray buf, int w, int h,
                                                                      jint saturation,
                                                                      jint contrast,
                                                                      jint brightness) {

    //读取int数组并转为Mat类型
    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (NULL == cbuf) {
        return 0;
    }
    cv::Mat imgData(h, w, CV_8UC4, (unsigned char *) cbuf);
    int size = w * h;
    CVWrapper wrapper = CVWrapper();
//    cv::cvtColor(imgData, imgData, CV_RGB2BGR);
    cv::Mat resultMat = wrapper.editPano(imgData, saturation, contrast, brightness);
//    cv::cvtColor(resultMat, resultMat, CV_BGR2RGB);
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (const jint *) resultMat.data);
    env->ReleaseIntArrayElements(buf, cbuf, 0);

    return result;
}


extern "C" void Java_opencv_OpencvLib_addPanoramaSkyNative(JNIEnv *env, jobject instance,
                                                           jstring panorPath_,
                                                           jstring skyPath_,
                                                           jstring savePath_, jfloat hight_,
                                                           jint baohedu, jint duibidu,
                                                           jint liangdu) {

    size_t panorPathLength = env->GetStringLength(panorPath_);
    char *panorPath = (char *) malloc(panorPathLength + 1);
    env->GetStringUTFRegion(panorPath_, 0, panorPathLength, panorPath);
    panorPath[panorPathLength] = '\0';

    LOGE("全景图路径 = %d", panorPath_);
    LOGE("全景图路径 = %d", panorPath);
    size_t savePathLength = env->GetStringLength(savePath_);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(savePath_, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';
    LOGE("保存路径 = %d", savePath);

    size_t skyPathLength = env->GetStringLength(skyPath_);
    char *skypath = (char *) malloc(skyPathLength + 1);
    env->GetStringUTFRegion(skyPath_, 0, skyPathLength, skypath);
    skypath[skyPathLength] = '\0';
    LOGE("全景图路径 = %d", skyPath_);
    LOGE("天空路径 = %d", skypath);

    CVWrapper wrapper = CVWrapper();
    wrapper.addPanoSky(panorPath, skypath, hight_, baohedu, duibidu, liangdu, savePath);

//    const char *skyPath = env->GetStringUTFChars(skyPath_, 0);
//    const char *savePath = env->GetStringUTFChars(savePath_, 0);

    // TODO

//    env->ReleaseStringUTFChars(skyPath_, skyPath);
//    env->ReleaseStringUTFChars(savePath_, savePath);
}

extern "C" void Java_opencv_OpencvLib_rotatingPanorImgNative(JNIEnv *env, jobject instance,
                                                             jstring panorPath_) {

    size_t panorPathLength = env->GetStringLength(panorPath_);
    char *panorPath = (char *) malloc(panorPathLength + 1);
    env->GetStringUTFRegion(panorPath_, 0, panorPathLength, panorPath);
    panorPath[panorPathLength] = '\0';

    LOGE("全景图路径 = %d", panorPath_);
    LOGE("全景图路径 = %d", panorPath);

    g_ctx.javaCallbackClass = env->GetObjectClass(instance);
    g_ctx.javaCallbackInstance = env->NewGlobalRef(instance);
    jclass javaClass = env->GetObjectClass(instance);
    jmethodID updateProgressID = env->GetMethodID(javaClass, "updateProgress", "(I)V");
    jmethodID resultID = env->GetMethodID(javaClass, "result", "(I)V");
    if (updateProgressID) {
        g_ctx.mainMethodId1 = updateProgressID;
    }
    if (resultID) {
        g_ctx.mainMethodId2 = resultID;
    }

    CVWrapper wrapper = CVWrapper();
    if (updateProgressID != NULL) {
//        wrapper->progressCallback = receiveProgressAfter; //进度回调 暂时去掉
    }

    if (resultID != NULL) {
        wrapper.errorCallback = resultAfter;
    }
    wrapper.fanzhuan(panorPath, panorPath);

//    const char *skyPath = env->GetStringUTFChars(skyPath_, 0);
//    const char *savePath = env->GetStringUTFChars(savePath_, 0);

    // TODO

//    env->ReleaseStringUTFChars(skyPath_, skyPath);
//    env->ReleaseStringUTFChars(savePath_, savePath);
}


extern "C" void Java_opencv_OpencvLib_createPanoramaNative(JNIEnv *env, jobject instance, jint argc,
                                                           jobjectArray argv, jstring path,
                                                           jstring create, jfloat retry) {
    size_t savePathLength = env->GetStringLength(path);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(path, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';


    size_t createPathLength = env->GetStringLength(create);
    char *createPath = (char *) malloc(createPathLength + 1);
    env->GetStringUTFRegion(create, 0, createPathLength, createPath);
    createPath[createPathLength] = '\0';


    const char *imagePaths[argc];
    for (int i = 0; i < argc; ++i) {
        jstring sPath = (jstring) env->GetObjectArrayElement(argv, i);
        size_t imagePathLength = env->GetStringLength(sPath);
        char *imagePath = (char *) malloc(imagePathLength + 1);
        env->GetStringUTFRegion(sPath, 0, imagePathLength, imagePath);
        imagePath[imagePathLength] = '\0';
        imagePaths[i] = imagePath;
    }
    g_ctx.javaCallbackClass = env->GetObjectClass(instance);
    g_ctx.javaCallbackInstance = env->NewGlobalRef(instance);
    jclass javaClass = env->GetObjectClass(instance);
    jmethodID updateProgressID = env->GetMethodID(javaClass, "updateProgress", "(I)V");
    jmethodID resultID = env->GetMethodID(javaClass, "result", "(I)V");
    if (updateProgressID) {
        g_ctx.mainMethodId1 = updateProgressID;
    }
    if (resultID) {
        g_ctx.mainMethodId2 = resultID;
    }


    CVWrapper wrapper = CVWrapper();
    if (updateProgressID != NULL) {
//        wrapper->progressCallback = receiveProgressAfter; //进度回调 暂时去掉
    }

    if (resultID != NULL) {
        wrapper.errorCallback = resultAfter;
    }
    wrapper.createPano(argc, imagePaths, savePath, createPath, retry);
    wrapper.client = NULL;
    wrapper = NULL;
}

extern "C" void
Java_opencv_OpencvLib_createGuangjiaoPanoramaNative(JNIEnv *env, jobject instance, jint argc,
                                                    jobjectArray argv, jstring path,
                                                    jstring create, jfloat retry) {
    size_t savePathLength = env->GetStringLength(path);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(path, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';


    size_t createPathLength = env->GetStringLength(create);
    char *createPath = (char *) malloc(createPathLength + 1);
    env->GetStringUTFRegion(create, 0, createPathLength, createPath);
    createPath[createPathLength] = '\0';


    const char *imagePaths[argc];
    for (int i = 0; i < argc; ++i) {
        jstring sPath = (jstring) env->GetObjectArrayElement(argv, i);
        size_t imagePathLength = env->GetStringLength(sPath);
        char *imagePath = (char *) malloc(imagePathLength + 1);
        env->GetStringUTFRegion(sPath, 0, imagePathLength, imagePath);
        imagePath[imagePathLength] = '\0';
        imagePaths[i] = imagePath;
    }
    g_ctx.javaCallbackClass = env->GetObjectClass(instance);
    g_ctx.javaCallbackInstance = env->NewGlobalRef(instance);
    jclass javaClass = env->GetObjectClass(instance);
    jmethodID updateProgressID = env->GetMethodID(javaClass, "updateProgress", "(I)V");
    jmethodID resultID = env->GetMethodID(javaClass, "result", "(I)V");
    if (updateProgressID) {
        g_ctx.mainMethodId1 = updateProgressID;
    }
    if (resultID) {
        g_ctx.mainMethodId2 = resultID;
    }

    CVWrapper wrapper = CVWrapper();
    if (updateProgressID != NULL) {
//        wrapper->progressCallback = receiveProgressAfter; //进度回调 暂时去掉
    }

    if (resultID != NULL) {
        wrapper.errorCallback = resultAfter;
    }
    wrapper.createGuangijaoPano(argc, imagePaths, savePath, createPath, retry);
    wrapper.fanzhuan(savePath, savePath);
    if (g_ctx.mainMethodId2 != NULL) {
        env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.mainMethodId2, 0);
    }
    wrapper.client = NULL;
    wrapper = NULL;
}

extern "C" jstring
Java_opencv_OpencvLib_compressedImgNative(JNIEnv *env, jclass instance, jstring url_,
                                          jstring outputPath_) {
    size_t inputUrlLen = env->GetStringLength(url_);
    char *inputUrl = (char *) malloc(inputUrlLen + 1);
    env->GetStringUTFRegion(url_, 0, inputUrlLen, inputUrl);
    inputUrl[inputUrlLen] = '\0';


    size_t outputLen = env->GetStringLength(outputPath_);
    char *outputUrl = (char *) malloc(outputLen + 1);
    env->GetStringUTFRegion(outputPath_, 0, outputLen, outputUrl);
    outputUrl[outputLen] = '\0';

    CVWrapper wrapper = CVWrapper();
    wrapper.compressedImg(inputUrl, outputUrl);
    wrapper.client = NULL;
    wrapper = NULL;

    return env->NewStringUTF(outputUrl);
}

void resultAfter(int err) {
    JavaVM *javaVM = g_ctx.javaVM;
    JNIEnv *env;
    jint res = javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = javaVM->AttachCurrentThread(&env, NULL);
        if (JNI_OK != res) {
            LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
            return;
        }
    }
    LOGD("resultAfter1==> %s", "回调");
    if (g_ctx.mainMethodId2 != NULL) {
        env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.mainMethodId2, err);
    }
    LOGD("resultAfter==> %s", "回调");

}

void receiveProgressAfter(int progress) {
    JavaVM *javaVM = g_ctx.javaVM;
    JNIEnv *env;
    jint res = javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = javaVM->AttachCurrentThread(&env, NULL);
        if (JNI_OK != res) {
            LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
            return;
        }
    }

    if (g_ctx.mainMethodId1 != NULL) {
        env->CallVoidMethod(g_ctx.javaCallbackInstance, g_ctx.mainMethodId1, progress);
    }

}

static inline void crc_acc(uint8_t data, uint16_t *accum) {
    uint8_t temp;

    temp = data ^ (uint8_t) (*accum);
    temp ^= temp << 4;
    *accum = (*accum >> 8) ^ ((uint16_t) temp << 8) ^ ((uint16_t) temp << 3) ^
             ((uint16_t) temp >> 4);
}

uint16_t seq_crc(const uint8_t *seq) {
    uint16_t acc = 0xffff;
    int size = SSIDLEN;

    while (size--)
        crc_acc(*seq++, &acc);

    crc_acc(0, &acc);

    return acc;
}

extern "C" bool Java_opencv_OpencvLib_duipingNative(JNIEnv *env, jobject instance, jstring ssid) {

    pid_t fd;
    int ret = 0;

    int sockfd, connsock;
    unsigned long time1;
    unsigned long time2;
    struct timeval time_val;
    //char *ip;
    struct sockaddr_in serveraddr;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    bzero(&serveraddr, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET; //÷∏∂® π”√µƒÕ®—∂–≠“È◊Â
    serveraddr.sin_port = htons(1234);//÷∏∂®“™¡¨Ω”µƒ∑˛ŒÒ∆˜µƒ∂Àø⁄
    inet_pton(AF_INET, "192.168.42.13", &serveraddr.sin_addr);
    LOGD("begin to connect socket\n");
    gettimeofday(&time_val, NULL);
    time1 = time_val.tv_sec;
    ret = connect(sockfd, (struct sockaddr *) &serveraddr, sizeof(serveraddr)); //¡¨Ω”∑˛ŒÒ∆˜
    gettimeofday(&time_val, NULL);
    time2 = time_val.tv_sec;
    LOGD("The connect time is %ld\n", time2 - time1);
    if (ret < 0)
        return false;

    LOGD("ssid %s\n", ssid);

    size_t ssidLength = env->GetStringLength(ssid);
    char *ossidBytes = (char *) malloc(ssidLength);
    uint8_t * ssidBytes = (uint8_t *) malloc(ssidLength);
    env->GetStringUTFRegion(ssid, 0, ssidLength, ossidBytes);
    memcpy(ssidBytes, ossidBytes, ssidLength);
    if (ssidLength < SSIDLEN) {
        return false;
    }

    size_t sndLength = ssidLength + 3;
    uint8_t * sndBytes = (uint8_t *) malloc(sndLength);

    uint16_t crc = seq_crc(ssidBytes);
    uint8_t lcrc = crc & 0xff;
    uint8_t hcrc = crc >> 8 & 0xff;
    uint8_t pre = 0x32;

    memcpy(sndBytes, &pre, 1);
    memcpy(sndBytes + 1, ssidBytes, ssidLength);
    memcpy(sndBytes + ssidLength + 1, &lcrc, 1);
    memcpy(sndBytes + ssidLength + 1 + 1, &hcrc, 1);

    ssize_t s = send(sockfd, sndBytes, 9, 0);

    if (s <= 0) {
        return false;
    }

    uint8_t recevBuf[10];
    ssize_t len = recv(sockfd, recevBuf, 10, 0);//接收服务器端信息
//    recevBuf[len]='\0';
    LOGD("收到的回应。。。。%d", recevBuf[0]); //打印服务器端信息 如果是0xA9 则是成功
    if (recevBuf[0] == 0x32) {
        return true;
    } else {
        return false;
    }
}







