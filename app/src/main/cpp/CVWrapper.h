//
//  CVWrapper.h
//  CVOpenTemplate
//
//  Created by Washe on 02/01/2013.
//  Copyright (c) 2013 foundry. All rights reserved.
//
#ifndef CVWrapper_hpp
#define CVWrapper_hpp

#include <string>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include "opencv2/core.hpp"
#include "opencv2/features2d.hpp"
#include "opencv2/stitching/warpers.hpp"
#include "opencv2/stitching/detail/matchers.hpp"
#include "opencv2/stitching/detail/motion_estimators.hpp"
#include "opencv2/stitching/detail/exposure_compensate.hpp"
#include "opencv2/stitching/detail/seam_finders.hpp"
#include "opencv2/stitching/detail/blenders.hpp"
#include "opencv2/stitching/detail/camera.hpp"
#include "opencv2/stitching/detail/timelapsers.hpp"
#include "opencv2/imgcodecs.hpp"

using namespace std;

typedef void (ProgressCallback)(int progress);

typedef void (ErrorCallback)(int errorCode);

class CVWrapper {
public:
    CVWrapper(bool try_use_gpu = true, bool can = false);

    // 用于回调
    const void *client;
    // 合成全景图会有合成进度之类,用于处理这些回调
    ProgressCallback *progressCallback;

    // 合成全景图会有合成错误,用于处理这些回调
    ErrorCallback *errorCallback;

    // 0 合成成功 -1 合成失败 该函数会阻塞当前线程直到返回
    void createPano(int imageNum, const char **imagePaths, const char *saveImagePath,
                    const char *createPano, float retry);

    // 0 合成成功 -1 合成失败 该函数会阻塞当前线程直到返回
    void createGuangijaoPano(int imageNum, const char **imagePaths, const char *saveImagePath,
                             const char *createPano, float retry);

    void fanzhuan(const char *sImagePath, const char *saveImagePath);

    void compressedImg(const char *inputUrl, const char *outputUrl);

    void addPanoSky(const char *pPath, const char *sPath, float sHeight, int baohedu,
                    int duibidu, int liangdu, const char *saPath);

    void rotatingPanorImg(const char *pPath, int angle, const char *saPath);

    void editPano(const char *sPath, int baohedu, int duibidu, int liangdu, const char *dPath);

    cv::Mat editPano(cv::Mat sImage, int baohedu, int duibidu, int liangdu);

    void cancel();


    // 参照图片：对于用户看得见的图片，比如显示在手机上的图片
    // x 左上角顶点距离参照图片的水平距离，y 左上角顶点距离参照图片的水平距离。
    // width 裁剪图片的宽度 height 裁剪图片的高度
    // screenWith 参照图片的宽度 screenheight 参照图片的高度
    // srcPath 原始图片路径 toPath裁剪图片保存路径
    void cutImage(float x, float y, float w, float h, float screenWith, float screenheight,
                  const char *srcPath, const char *toPath,
                  float scale);


//private:
//    bool isCancel;
//
//    double work_scale_;
//    double seam_scale_;
//    double seam_work_aspect_;
//    double warped_image_scale_;
//    double registr_resol_;
//    double seam_est_resol_;
//    double compose_resol_;
//    double conf_thresh_;
//    cv::Ptr<cv::detail::FeaturesFinder> features_finder_;
//    cv::Ptr<cv::detail::FeaturesMatcher> features_matcher_;
//    cv::UMat matching_mask_;
//    cv::Ptr<cv::detail::BundleAdjusterBase> bundle_adjuster_;
//    /* TODO OpenCV ABI 4.x
//     Ptr<detail::Estimator> estimator_;
//     */
//    bool do_wave_correct_;
//    cv::detail::WaveCorrectKind wave_correct_kind_;
//    cv::Ptr<cv::WarperCreator> warper_;
//    cv::Ptr<cv::detail::ExposureCompensator> exposure_comp_;
//    cv::Ptr<cv::detail::SeamFinder> seam_finder_;
//    cv::Ptr<cv::detail::Blender> blender_;
//
//    std::vector<cv::UMat> imgs_;
//    std::vector<std::vector<cv::Rect> > rois_;
//    std::vector<cv::Size> full_img_sizes_;
//    std::vector<cv::detail::ImageFeatures> features_;
//    std::vector<cv::detail::MatchesInfo> pairwise_matches_;
//    std::vector<cv::UMat> seam_est_imgs_;
//    std::vector<cv::detail::CameraParams> cameras_;
//
//
//    vector<cv::UMat> convertToUmat(cv::InputArrayOfArrays images);
};

#endif