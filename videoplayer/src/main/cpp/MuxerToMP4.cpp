//
//  MuxerToMP4.cpp
//  Flypie
//
//  Created by apple on 2019/10/10.
//  Copyright © 2019 Flypie. All rights reserved.
//

#include "MuxerToMP4.hpp"
#include "CPPLog.h"

void MuxerToMP4::encodeAndSaveToPath(AVFormatContext *wFormatCtx, AVCodecContext *codecCtx,
                                     AVFrame *frame) {
    int ret = avcodec_send_frame(codecCtx, frame);

    while (ret >= 0) {
        AVPacket *oupkt = av_packet_alloc();
        ret = avcodec_receive_packet(codecCtx, oupkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return;
        } else if (ret < 0) {
            return;
        }
        AVRational in = wFormatCtx->streams[0]->time_base;
        AVRational ou = codecCtx->time_base;
        oupkt->pts = av_rescale_q_rnd(oupkt->pts, ou, in, AV_ROUND_NEAR_INF);
        oupkt->dts = av_rescale_q_rnd(oupkt->dts, ou, in, AV_ROUND_NEAR_INF);
        ret = av_interleaved_write_frame(wFormatCtx, oupkt);
        av_packet_free(&oupkt);
    }
}

MuxerToMP4::MuxerToMP4(string inpath, string oupath)
        : fInpath(inpath), fOupath(oupath), pInformatCtx(NULL), pOuformatCtx(NULL),
          pEncodeCodecCtx(NULL), pDecodeCodecCtx(NULL), sws_ctx(NULL) {

}

MuxerToMP4::~MuxerToMP4() {
    LOGD("AVI===>MP4: 结束 1");
    if (pInformatCtx != NULL) {
        avformat_free_context(pInformatCtx);
    }
    LOGD("AVI===>MP4: 结束 2");
    if (pOuformatCtx) {
        avformat_free_context(pOuformatCtx);
    }
    LOGD("AVI===>MP4: 结束 3");
    if (pEncodeCodecCtx) {
        avcodec_free_context(&pEncodeCodecCtx);
    }
    LOGD("AVI===>MP4: 结束 4");
    if (pDecodeCodecCtx) {
        avcodec_free_context(&pDecodeCodecCtx);
    }
    LOGD("AVI===>MP4: 结束 5");
}

void MuxerToMP4::aviToMp4() {
    if (fInpath.size() == 0 || fOupath.size() == 0) {
        return;
    }
    const char *inpath = fInpath.c_str();
    const char *oupath = fOupath.c_str();
    LOGD("AVI===>MP4:inpath路径 = %s", inpath);
    LOGD("AVI===>MP4:oupath路径 = %s", oupath);
    AVCodec *pEncodec;
    AVCodec *pDecodec;

    // ==== 输入解码上下文 ======= ///
    int ret = avformat_open_input(&pInformatCtx, inpath, NULL, NULL);
    if (pInformatCtx == NULL) {
        return;
    }
    avformat_find_stream_info(pInformatCtx, NULL);
    int videoIndex = -1;
    for (int i = 0; i < pInformatCtx->nb_streams; i++) {
        if (pInformatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
            break;
        }
    }
    if (videoIndex == -1) {
        return;
    }
    pDecodec = avcodec_find_decoder(pInformatCtx->streams[videoIndex]->codecpar->codec_id);
    if (pDecodec == NULL) {
        return;
    }
    pDecodeCodecCtx = avcodec_alloc_context3(pDecodec);
    if (pDecodeCodecCtx == NULL) {
        return;
    }
    avcodec_parameters_to_context(pDecodeCodecCtx, pInformatCtx->streams[videoIndex]->codecpar);
    ret = avcodec_open2(pDecodeCodecCtx, pDecodec, NULL);
    if (ret < 0) {
        return;
    }
    // ==== 输入解码上下文 ======= ///

    // ==== 编码上下文 ===== //
    pEncodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (pEncodec == NULL) {
        return;
    }
    pEncodeCodecCtx = avcodec_alloc_context3(pEncodec);
    if (pEncodeCodecCtx == NULL) {
        return;
    }
    pEncodeCodecCtx->codec_id = AV_CODEC_ID_H264;
    pEncodeCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pEncodeCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
    pEncodeCodecCtx->bit_rate = 1.8 * 1000000;
    pEncodeCodecCtx->width = pDecodeCodecCtx->width;
    pEncodeCodecCtx->height = pDecodeCodecCtx->height;
    pEncodeCodecCtx->time_base = av_make_q(1, 24);
    pEncodeCodecCtx->framerate = (AVRational) {24, 1};
    pEncodeCodecCtx->gop_size = 10;
    pEncodeCodecCtx->max_b_frames = 0;
    av_opt_set(pEncodeCodecCtx->priv_data, "preset", "slow", 0);
    pEncodeCodecCtx->flags |= AV_CODEC_FLAG2_LOCAL_HEADER;
    avformat_alloc_output_context2(&pOuformatCtx, NULL, NULL, oupath);
    if (pOuformatCtx == NULL) {
        return;
    }
    AVStream *oustream = avformat_new_stream(pOuformatCtx, NULL);
    if (pOuformatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        pEncodeCodecCtx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    ret = avcodec_open2(pEncodeCodecCtx, pEncodec, NULL);
    if (ret < 0) {
        return;
    }
    avcodec_parameters_from_context(oustream->codecpar, pEncodeCodecCtx);

    if (!(pOuformatCtx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&pOuformatCtx->pb, oupath, AVIO_FLAG_WRITE);
        if (ret < 0) {
            return;
        }
    }

    ret = avformat_write_header(pOuformatCtx, NULL);
    if (ret < 0) {
        return;
    }
    // ==== 编码上下文 ===== //


    AVFrame *inFrame = av_frame_alloc();
    AVFrame *ouFrame = av_frame_alloc();
    ouFrame->format = pEncodeCodecCtx->pix_fmt;
    ouFrame->width = pEncodeCodecCtx->width;
    ouFrame->height = pEncodeCodecCtx->height;
    av_frame_get_buffer(ouFrame, 0);
    av_frame_make_writable(ouFrame);

    sws_ctx = sws_getContext(pDecodeCodecCtx->width, pDecodeCodecCtx->height,
                             pDecodeCodecCtx->pix_fmt, pEncodeCodecCtx->width,
                             pEncodeCodecCtx->height, pEncodeCodecCtx->pix_fmt, SWS_BILINEAR, NULL,
                             NULL, NULL);
    if (sws_ctx == NULL) {
        return;
    }
    int i = 0;
    while (true) {
        AVPacket *inpkt = av_packet_alloc();
        ret = av_read_frame(pInformatCtx, inpkt);
        if (ret < 0) {
            encodeAndSaveToPath(pOuformatCtx, pEncodeCodecCtx, NULL);
            av_packet_free(&inpkt);
            break;
        }

        if (inpkt->stream_index != videoIndex) {
            av_packet_free(&inpkt);
            continue;
        }

        ret = avcodec_send_packet(pDecodeCodecCtx, inpkt);
        ret = avcodec_receive_frame(pDecodeCodecCtx, inFrame);

        ret = sws_scale(sws_ctx, inFrame->data, inFrame->linesize, 0, inFrame->height,
                        ouFrame->data, ouFrame->linesize);
        if (ret < 0) {
            continue;
        }
        ouFrame->pts = i++;

        encodeAndSaveToPath(pOuformatCtx, pEncodeCodecCtx, ouFrame);
        av_packet_free(&inpkt);
    }

    ret = av_write_trailer(pOuformatCtx);
}
