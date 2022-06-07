//
// Created by 飞拍科技 on 2019/3/2.
//

#include "SoftDecoder.h"
#include "CPPLog.h"

SoftDecoder::SoftDecoder(VideoType type):fType(type),fVideoDecoder(NULL),fCurrentIndex(0)
{
    LOGD("SoftDecoder()");
    resetDecoder(type);
    for (int i=0; i<CACHE_YUV_FRAME_NUMBER; i++) {
        fYUVFrames[i] = (VideoFrameYUV*)malloc(sizeof(VideoFrameYUV));
        memset(fYUVFrames[i],0,sizeof(VideoFrameYUV));
    }
}

SoftDecoder::~SoftDecoder()
{
    for (int i=0; i<CACHE_YUV_FRAME_NUMBER; i++) {
        if (fYUVFrames[i]) {
            if (fYUVFrames[i]->luma) {
                free(fYUVFrames[i]->luma);
                fYUVFrames[i]->luma = NULL;
            }
            if (fYUVFrames[i]->chromaB) {
                free(fYUVFrames[i]->chromaB);
                fYUVFrames[i]->chromaB = NULL;
            }
            if (fYUVFrames[i]->chromaR) {
                free(fYUVFrames[i]->chromaR);
                fYUVFrames[i]->chromaR = NULL;
            }
            free(fYUVFrames[i]);
            fYUVFrames[i] = NULL;
        }
    }

    if (fVideoDecoder != NULL) {
        delete fVideoDecoder;
        fVideoDecoder = NULL;
    }
}

void SoftDecoder::setDecoderType(VideoType type)
{
    if (fType == type) {
        return;
    }
    fType = type;
    resetDecoder(type);
}

void SoftDecoder::setRenderWindow(ANativeWindow *window)
{

}

VideoFrameYUV* SoftDecoder::decode(uint8_t* compressedData,int length)
{
    if (compressedData == NULL || length <= 0) {
        return NULL;
    }
    bool result = fVideoDecoder->decodeVideo(compressedData,length);
    VideoFrameYUV *returnYUVFrame = NULL;
    if (result) {   // 解码成功，则将解码后的数据取出来
        returnYUVFrame = fYUVFrames[fCurrentIndex];
        fVideoDecoder->getYUVFrame(returnYUVFrame);

        fCurrentIndex++;
        if (fCurrentIndex % CACHE_YUV_FRAME_NUMBER == 0) {
            fCurrentIndex=0;
        }
        return returnYUVFrame;
    }
    LOGD("解码失败");
    return returnYUVFrame;
}

void SoftDecoder::resetDecoder(VideoType type)
{
    if (fVideoDecoder != NULL) {
        delete fVideoDecoder;
        fVideoDecoder = NULL;
    }
    fVideoDecoder = new VideoProcess(type);
}