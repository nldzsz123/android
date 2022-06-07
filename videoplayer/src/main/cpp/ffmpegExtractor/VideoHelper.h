//
//  VideoHelper.h
//  study
//
//  Created by 飞拍科技 on 2019/1/6.
//  Copyright © 2019 飞拍科技. All rights reserved.
//

#ifndef VideoHelper_h
#define VideoHelper_h

#include <stdio.h>

#ifndef OUT
#define OUT
#endif
#ifndef IN
#define IN
#endif

////For acquiring pre-construction I frame struct
typedef struct _DummyIframeInfo{
    int frame_width;
    int frame_height;
    int fps;
    int encoder_type; //H264EncoderType
}PrebuildIframeInfo;

typedef char* (*loadPrebuildIframePathPtr)(uint8_t* buffer, int in_buffer_size, PrebuildIframeInfo info);
typedef int (*loadPrebuildIframeOverridePtr)(uint8_t* buffer, int in_buffer_size, PrebuildIframeInfo info);;

extern loadPrebuildIframePathPtr g_loadPrebuildIframePathFunc;
extern loadPrebuildIframeOverridePtr g_loadPrebuildIframeOverrideFunc;

enum HEVCNALUnitType {
    H265_NAL_TRAIL_N    = 0,
    H265_NAL_TRAIL_R    = 1,
    H265_NAL_TSA_N      = 2,
    H265_NAL_TSA_R      = 3,
    H265_NAL_STSA_N     = 4,
    H265_NAL_STSA_R     = 5,
    H265_NAL_RADL_N     = 6,
    H265_NAL_RADL_R     = 7,
    H265_NAL_RASL_N     = 8,
    H265_NAL_RASL_R     = 9,
    H265_NAL_BLA_W_LP   = 16,
    H265_NAL_BLA_W_RADL = 17,
    H265_NAL_BLA_N_LP   = 18,
    H265_NAL_IDR_W_RADL = 19,
    H265_NAL_IDR_N_LP   = 20,
    H265_NAL_CRA_NUT    = 21,
    H265_NAL_VPS        = 32,
    H265_NAL_SPS        = 33,
    H265_NAL_PPS        = 34,
    H265_NAL_AUD        = 35,
    H265_NAL_EOS_NUT    = 36,
    H265_NAL_EOB_NUT    = 37,
    H265_NAL_FD_NUT     = 38,
    H265_NAL_SEI_PREFIX = 39,
    H265_NAL_SEI_SUFFIX = 40,
};

enum H264NALUnitType {
    H264_NAL_SLICE    = 1,
    H264_NAL_DPA      = 2,
    H264_NAL_DPB      = 3,
    H264_NAL_DPC      = 4,
    H264_NAL_IDR      = 5,
    H264_NAL_SEI      = 6,
    H264_NAL_SPS      = 7,
    H264_NAL_PPS      = 8,
    H264_NAL_AUD      = 9,
    H264_NAL_EOSEQ    = 10,
    H264_NAL_EOSTREAM = 11,
    H264_NAL_FILL     = 12,
#if (MVC_EXTENSION_ENABLE)
    H264_NAL_PREFIX   = 14,
    H264_NAL_SUB_SPS  = 15,
    H264_NAL_SLC_EXT  = 20,
    H264_NAL_VDRD     = 24
#endif
};

//h264 sps from ffmpeg
typedef struct SPS {
    unsigned int sps_id;
    int profile_idc;
    int level_idc;
    int chroma_format_idc;
    int transform_bypass;              ///< qpprime_y_zero_transform_bypass_flag
    int log2_max_frame_num;            ///< log2_max_frame_num_minus4 + 4
    int poc_type;                      ///< pic_order_cnt_type
    int log2_max_poc_lsb;              ///< log2_max_pic_order_cnt_lsb_minus4
    int delta_pic_order_always_zero_flag;
    int offset_for_non_ref_pic;
    int offset_for_top_to_bottom_field;
    int poc_cycle_length;              ///< num_ref_frames_in_pic_order_cnt_cycle
    int ref_frame_count;               ///< num_ref_frames
    int gaps_in_frame_num_allowed_flag;
    int mb_width;                      ///< pic_width_in_mbs_minus1 + 1
    int mb_height;                     ///< pic_height_in_map_units_minus1 + 1
    int frame_mbs_only_flag;
    int mb_aff;                        ///< mb_adaptive_frame_field_flag
    int direct_8x8_inference_flag;
    int crop;                          ///< frame_cropping_flag
    
    /* those 4 are already in luma samples */
    unsigned int crop_left;            ///< frame_cropping_rect_left_offset
    unsigned int crop_right;           ///< frame_cropping_rect_right_offset
    unsigned int crop_top;             ///< frame_cropping_rect_top_offset
    unsigned int crop_bottom;          ///< frame_cropping_rect_bottom_offset
    int vui_parameters_present_flag;
    struct{
        int num; ///< numerator
        int den; ///< denominator
    } sar;
    int video_signal_type_present_flag;
    int full_range;
    int colour_description_present_flag;
    int color_primaries;
    int color_trc;
    int colorspace;
    int timing_info_present_flag;
    unsigned long num_units_in_tick;
    unsigned long time_scale;
    int fixed_frame_rate_flag;
    short offset_for_ref_frame[256]; // FIXME dyn aloc?
    int bitstream_restriction_flag;
    int num_reorder_frames;
    int scaling_matrix_present;
    unsigned char scaling_matrix4[6][16];
    unsigned char scaling_matrix8[6][64];
    int nal_hrd_parameters_present_flag;
    int vcl_hrd_parameters_present_flag;
    int pic_struct_present_flag;
    int time_offset_length;
    int cpb_cnt;                          ///< See H.264 E.1.2
    int initial_cpb_removal_delay_length; ///< initial_cpb_removal_delay_length_minus1 + 1
    int cpb_removal_delay_length;         ///< cpb_removal_delay_length_minus1 + 1
    int dpb_output_delay_length;          ///< dpb_output_delay_length_minus1 + 1
    int bit_depth_luma;                   ///< bit_depth_luma_minus8 + 8
    int bit_depth_chroma;                 ///< bit_depth_chroma_minus8 + 8
    int residual_color_transform_flag;    ///< residual_colour_transform_flag
    int constraint_set_flags;             ///< constraint_set[0-3]_flag
    //int new;                              ///< flag to keep track if the decoder context needs re-init due to changed SPS
} SPS;

typedef struct{
    int first_mb_in_slice;
    int slice_type;
    //frame_num is an ID that used to distinguish different frames. It is not counter.
    int frame_num;
} H264SliceHeaderSimpleInfo;

/**
 *  Decode seq data.
 *
 *  @param buf In sps buffer.
 *  @param nLen In Buffer size.
 *  @param out_width Out mb width.
 *  @param out_height Out mb hegiht.
 *  @param framerate Out The frame rate.
 *  @param out_sps Out sps data.
 *
 *  @return `0` if it is set successfully.
 */
int    h264_decode_seq_parameter_set_out(IN unsigned char * buf,IN unsigned int nLen,OUT int * out_width,OUT int * out_height,OUT int *framerate,OUT SPS* out_sps);

/**
 *  Decode a slice header.
 *
 *  @param buf In sps buffer.
 *  @param nLen In Buffer size.
 *  @param out_sps Out Sps data.
 *  @param out_info Out the slice header info.
 *
 *  @return `0` if it is decode successfully.
 */
int h264_decode_slice_header(IN unsigned char * buf,IN unsigned int nLen,OUT SPS* out_sps,OUT H264SliceHeaderSimpleInfo* out_info);

/**
 *  Search the end position of nalu header.
 *
 *  @param buf In Frame data.
 *  @param size IN Frame size.
 *
 *  @return The search position.
 */
int findNextNALStartCodeEndPos(IN uint8_t* buf, IN int size);

/**
 *  Search the start position of nalu header.
 *
 *  @param buf In Frame data.
 *  @param size In Frame size.
 *
 *  @return The search position.
 */
int findNextNALStartCodePos(IN uint8_t* buf, IN int size);

/**
 *  Find the sps and pps frame.
 *
 *  @param  buf In buffer data.
 *  @param  size In buffer size.
 *  @param  out_SPS Out sps result.
 *  @param  out_SPSLen Out sps size.
 *  @param  out_PPS Out pps result.
 *  @param  out_PPSLen Out pps size.
 */
int find_SPS_PPS(IN uint8_t* buf,IN int size,OUT uint8_t* out_SPS,OUT int* out_SPSLen,OUT uint8_t* out_PPS,OUT int* out_PPSLen);

/**
 *  Attempts to load pre-constructed i frame from disk
 *
 *  @param buf Out the i frame data.
 *  @param size In In buffer size.
 *  @param info In the i frame info.
 *
 *  @return If less than `0` mean the buffer have no enough size, '0' No correspond i frame, more than `0` the size of get.
 */
int loadPrebuildIframe(OUT uint8_t* buf,IN int size,IN PrebuildIframeInfo info);

#endif /* VideoHelper_h */
