//
// Created by apple on 2019-09-19.
//

#include <jni.h>
#include <string>
#include <iostream>
#include "VideoStab.h"
#include "opencv2/videostab.hpp"
#include "opencv2/videoio.hpp"
#include "opencv2/video.hpp"
#include "opencv2/imgcodecs.hpp"
#include <unistd.h>
#include <fcntl.h>

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

extern "C" jintArray
Java_com_flypie_videostab_Videostab_picsToVideo(JNIEnv *env, jclass jclass, jobjectArray filePaths,
                                                jint filesSize,
                                                jstring outPath_, jint delayResolution) {
    size_t savePathLength = env->GetStringLength(outPath_);
    char *savePath = (char *) malloc(savePathLength + 1);
    env->GetStringUTFRegion(outPath_, 0, savePathLength, savePath);
    savePath[savePathLength] = '\0';

    const char *picPaths[filesSize];
    for (int i = 0; i < filesSize; ++i) {
        jstring picPath = (jstring) env->GetObjectArrayElement(filePaths, i);
        size_t picPathLen = env->GetStringLength(picPath);
        char *imagePath = (char *) malloc(picPathLen + 1);
        env->GetStringUTFRegion(picPath, 0, picPathLen, imagePath);
        imagePath[picPathLen] = '\0';
        picPaths[i] = imagePath;
    }

    vector<Mat> imgs;
    Mat img, smallImg;;

    string orgpath = string(savePath);
    int pos = orgpath.find_last_of('/');

    string filename(orgpath.substr(pos + 1));
    string path(orgpath.substr(0, pos));
    string tmpsavepath = path + string("/tmp") + filename;
    if (access(tmpsavepath.c_str(), F_OK) == 0) {
        remove(tmpsavepath.c_str());
    }

    int fps = 24;      //设置输出视频的帧率
    VideoWriter writer;
    for (int i = 0; i < filesSize; i++) {
        string pic_path = picPaths[i];
        img = cv::imread(pic_path);
        if (!img.rows) {
            continue;
        }

        Size sz = img.size();
        double rate = sz.height / 540;//原图压缩比例
        if (rate >= 1.0) {
            sz.height = (int) (sz.height / rate);
            sz.width = (int) (sz.width / rate);
            resize(img, smallImg, sz, 0, 0, 3);
        }

        if (!writer.isOpened()) {
            writer.open(tmpsavepath, VideoWriter::fourcc('M', 'J', 'P', 'G'),
                        fps, smallImg.size());
        }
        writer.write(smallImg);
    }
    writer.release();

//    int width=0;
//    int height=0;
    // 增稳
    Ptr<IFrameSource> stabFrames;
    cacStabVideo(stabFrames, tmpsavepath, orgpath);
    stabFrames.release();
    if (access(tmpsavepath.c_str(), F_OK) == 0) {
        remove(tmpsavepath.c_str());
    }

//    jintArray returnarray = env->NewIntArray(2);
//    jint* elementp = env->GetIntArrayElements(returnarray, NULL);
//    elementp[0]=width;
//    elementp[1]=height;
//    env->ReleaseIntArrayElements(returnarray, elementp, 0);

    return nullptr;
}