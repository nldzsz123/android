//
// Created by 飞拍科技 on 2019/3/2.
//

#ifndef FLYPIEFORANDROID_DEOCDER_H
#define FLYPIEFORANDROID_DEOCDER_H

#include <ffmpegExtractor/CommonDefine.h>
#include <jni.h>
#include <android/native_window.h>

/** 解码器的抽象类
 *
 * */
class Decoder{
public:

    Decoder();
    virtual ~Decoder();

    // 设置解码器类型
    virtual void setDecoderType(VideoType type) = 0;

    // 解码完成后的回调
    typedef void DecodeCallback(void* delegate,VideoFrameYUV *yuvFrame);
    void setDecodeCallback(void *delegate,DecodeCallback *cb);

    // 设置渲染 窗口 实现了此函数则可以直接渲染在此窗口上
    virtual void setRenderWindow(ANativeWindow *window) = 0;

    // 解码 同步函数
    virtual VideoFrameYUV* decode(uint8_t* compressedData,int length) = 0;



private:
    // 回调对象
    void *fDelegate;
    // 回调函数
    DecodeCallback *fDecodeCallback;
};

#endif //FLYPIEFORANDROID_DEOCDER_H
