//
//  CommonDefine.h
//  study
//
//  Created by 飞拍科技 on 2019/1/3.
//  Copyright © 2019 飞拍科技. All rights reserved.
//

#ifndef CommonDefine_h
#define CommonDefine_h

#include <stdint.h>

// 视频的显示方向，横屏和竖屏
typedef enum {
    VideoRotationTypeLandscape = 1,
    VideoRotationTypePortrait
}VideoRotationType;

// 视频解压后的YUV数据格式
typedef enum {
    VideFrameTypeYUV420Planer = 1,
    VideFrameTypeYUV420BiPlaner
}VideFrameType;

typedef enum {
    VideoTypeH264 = 1,
    VideoTypeH265
}VideoType;

typedef struct
{
    /**
     *  The following three ptrs has different meanings when the encoding format
     *  is different.
     *  - YUV Planer: Y in `luma`, U in `chromaB` and V in `chromaR`
     *  - YUV Semi-Planer: y in `luma` and CrCb in `chromaB`
     *  - RGB: RGB value is stored in `luma`
     *
     *  When fastupload is enabled, `luma`, `chromaB` and `chromaR` may be nil
     *  and the pixel information can be accessed in `cv_pixelbuffer_fastupload`.
     */
    uint8_t *luma;
    uint8_t *chromaB;
    uint8_t *chromaR;
    
    uint8_t frameType; //VideFrameType
    uint8_t rotationType;   //VideRotationType
    
    int width, height;
    
    //slice data，`0` indicates the default value.
    int lumaSlice, chromaBSlice, chromaRSlice;
    
    // It is only valid when fastupload is enabled.
    void* cv_pixelbuffer_fastupload;
    
}VideoFrameYUV;

#pragma pack (1)
typedef struct{
    uint32_t keyFrame:4;    // 是否关键帧
    uint32_t frameType:4;    // 编码类型
    uint32_t frameSize:24;   // 帧的大小
    uint32_t frameWidth:16;  // 帧的宽度
    uint32_t frameHeight:16; // 帧的高度
    uint8_t  frameData[0];   //linux系统 gcc编译器支持的;完整的压缩的视频数据
}VideoPacket;
#pragma pack()

#define CPY_YUV_FRAME(dst, src, linesize, width, height) \
do{ \
if(dst == NULL || src == NULL || linesize < width || width <= 0)\
break;\
uint8_t * dd = (uint8_t* ) dst; \
uint8_t * ss = (uint8_t* ) src; \
int ll = linesize; \
int ww = width; \
int hh = height; \
for(int i = 0 ; i < hh ; ++i) \
{ \
memcpy(dd, ss, width); \
dd += ww; \
ss += ll; \
} \
}while(0)

#endif /* VideoCommon_h */
