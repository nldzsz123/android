//
//  MuxerToMP4.hpp
//  Flypie
//
//  Created by apple on 2019/10/10.
//  Copyright © 2019 Flypie. All rights reserved.
//

#ifndef MuxerToMP4_hpp
#define MuxerToMP4_hpp

#include <stdio.h>
#include <string>

using namespace std;
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/opt.h"
#include "libavutil/imgutils.h"
#include "libswscale/swscale.h"
#include "libavutil/timestamp.h"
}

class MuxerToMP4
{
public:
    MuxerToMP4(string inpath,string oupath);
    ~MuxerToMP4();
    
    void aviToMp4();
    
private:
    string fInpath;
    string fOupath;
    
    AVFormatContext *pInformatCtx;
    AVFormatContext *pOuformatCtx;
    AVCodecContext  *pEncodeCodecCtx;
    AVCodecContext  *pDecodeCodecCtx;
    struct SwsContext *sws_ctx;
    
    void encodeAndSaveToPath(AVFormatContext *wFormatCtx,AVCodecContext* codecCtx,AVFrame*frame);
};

#endif /* MuxerToMP4_hpp */
