//
//  CVWrapper.m
//  CVOpenTemplate
//
//  Created by Washe on 02/01/2013.
//  Copyright (c) 2013 foundry. All rights reserved.
//
#include <sys/time.h>
#include <ios>

#include "CVWrapper.h"
#include "opencv2/stitching.hpp"
#include "opencv2/videostab.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc.hpp"

using namespace cv;
using namespace cv::detail;
using namespace cv::videostab;

#define ENABLE_LOG 1
#if ENABLE_LOG
#ifdef ANDROID

#include <android/log.h>
#include <opencv2/videoio.hpp>
#include <opencv/cv.hpp>


#define LOG_STITCHING_MSG(msg) \
do { \
std::stringstream _os; \
_os << msg; \
__android_log_print(ANDROID_LOG_DEBUG,"全景合成", "%s", _os.str().c_str()); \
} while(0);
#else
#include <iostream>
#define LOG_STITCHING_MSG(msg) for(;;) { std::cout <<getTimeFormat()<<" "<< msg; std::cout.flush(); break; }
#endif
#else
#define LOG_STITCHING_MSG(msg)
#endif

#define LOGD(msg) LOG_STITCHING_MSG(msg << std::endl);

char *getTimeFormat() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    struct tm *tm_local = localtime(&tv.tv_sec);
    char str_f_t[30];
    strftime(str_f_t, sizeof(str_f_t), "%G-%m-%d %H:%M:%S", tm_local);
    char *returnStr = new char[40]();
    sprintf(returnStr, "%s:%.0f", str_f_t, tv.tv_usec / 1000.0);
    return returnStr;
}

void CVWrapper::cancel() {
//    isCancel = true;
}

CVWrapper::CVWrapper(bool try_use_gpu, bool can) {

}

//vector<cv::UMat> CVWrapper::convertToUmat(InputArrayOfArrays images)
//{
//    vector<cv::UMat> returnImages;
//    images.getUMatVector(returnImages);
//
//    return returnImages;
//};

Stitcher::Status genpano(vector<Mat> imgs, Mat &pano, Rect &pano_size, const char *saveImagePath) {
//    Mat pano;
    Ptr<Stitcher> stitcher = Stitcher::create(Stitcher::PANORAMA, false);

    Stitcher::Status status = stitcher->stitch(imgs, pano);

    stitcher->blender_;
    Mat result, result_mask;
    stitcher->blender_->blend(pano, result_mask);
    stitcher->panno_size = stitcher->blender_->getPanoSize();//stitcher.blender_->pano_size;
    pano_size = Rect(0, stitcher->blender_->dst_.rows - stitcher->panno_size.height,
                     stitcher->panno_size.width, stitcher->panno_size.height);
    imwrite(saveImagePath, stitcher->blender_->dst_);

    return status;
}

bool checkInteriorExterior(const cv::Mat &mask, const cv::Rect &interiorBB, int &top, int &bottom,
                           int &left, int &right) {
    // return true if the rectangle is fine as it is!
    bool returnVal = true;

    cv::Mat sub = mask(interiorBB);

    unsigned int x = 0;
    unsigned int y = 0;

    // count how many exterior pixels are at the
    unsigned int cTop = 0; // top row
    unsigned int cBottom = 0; // bottom row
    unsigned int cLeft = 0; // left column
    unsigned int cRight = 0; // right column
    // and choose that side for reduction where mose exterior pixels occured (that's the heuristic)

    for (y = 0, x = 0; x < sub.cols; ++x) {
        // if there is an exterior part in the interior we have to move the top side of the rect a bit to the bottom
        if (sub.at<unsigned char>(y, x) == 0) {
            returnVal = false;
            ++cTop;
        }
    }

    for (y = sub.rows - 1, x = 0; x < sub.cols; ++x) {
        // if there is an exterior part in the interior we have to move the bottom side of the rect a bit to the top
        if (sub.at<unsigned char>(y, x) == 0) {
            returnVal = false;
            ++cBottom;
        }
    }

    for (y = 0, x = 0; y < sub.rows; ++y) {
        // if there is an exterior part in the interior
        if (sub.at<unsigned char>(y, x) == 0) {
            returnVal = false;
            ++cLeft;
        }
    }

    for (x = sub.cols - 1, y = 0; y < sub.rows; ++y) {
        // if there is an exterior part in the interior
        if (sub.at<unsigned char>(y, x) == 0) {
            returnVal = false;
            ++cRight;
        }
    }

    // that part is ugly and maybe not correct, didn't check whether all possible combinations are handled. Check that one please. The idea is to set `top = 1` iff it's better to reduce the rect at the top than anywhere else.
    if (cTop > cBottom) {
        if (cTop > cLeft)
            if (cTop > cRight)
                top = 1;
    } else if (cBottom > cLeft)
        if (cBottom > cRight)
            bottom = 1;

    if (cLeft >= cRight) {
        if (cLeft >= cBottom)
            if (cLeft >= cTop)
                left = 1;
    } else if (cRight >= cTop)
        if (cRight >= cBottom)
            right = 1;


    return returnVal;
}

bool sortX(cv::Point a, cv::Point b) {
    bool ret = false;
    if (a.x == a.x)
        if (b.x == b.x)
            ret = a.x < b.x;

    return ret;
}

bool sortY(cv::Point a, cv::Point b) {
    bool ret = false;
    if (a.y == a.y)
        if (b.y == b.y)
            ret = a.y < b.y;


    return ret;
}

Stitcher::Status
genCutpano(vector<Mat> imgs, Mat &pano, Rect &pano_size, const char *saveImagePath) {
    //Mat pano;
    Ptr<Stitcher> stitcher = Stitcher::create(Stitcher::PANORAMA, false);

    Stitcher::Status status = stitcher->stitch(imgs, pano);

    stitcher->blender_;
    Mat result, result_mask;
    stitcher->blender_->blend(pano, result_mask);
    stitcher->panno_size = stitcher->blender_->getPanoSize();//stitcher.blender_->pano_size;
    pano_size = Rect(0, stitcher->blender_->dst_.rows - stitcher->panno_size.height,
                     stitcher->panno_size.width, stitcher->panno_size.height);

    Mat input = stitcher->blender_->dst_;
    if (!input.rows) {
        return status;
    }
    // 转化为灰度图
    cv::Mat gray;
    cv::cvtColor(input, gray, CV_BGR2GRAY);

    // extract all the black background (and some interior parts maybe)
    cv::Mat mask = gray > 0;

    // 计算轮廓
    std::vector<std::vector<cv::Point> > contours;
    std::vector<cv::Vec4i> hierarchy;

    cv::findContours(mask, contours, hierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE,
                     cv::Point(0, 0));

    cv::Mat contourImage = cv::Mat::zeros(input.size(), CV_8UC3);

    //find contour with max elements
    // remark: in theory there should be only one single outer contour surrounded by black regions!!

    unsigned long maxSize = 0;
    unsigned int id = 0;
    for (unsigned int i = 0; i < contours.size(); ++i) {
        if (contours.at(i).size() > maxSize) {
            maxSize = contours.at(i).size();
            id = i;
        }
    }

    /// Draw filled contour to obtain a mask with interior parts
    cv::Mat contourMask = cv::Mat::zeros(input.size(), CV_8UC1);
    cv::drawContours(contourMask, contours, id, cv::Scalar(255), -1, 8, hierarchy, 0, cv::Point());

    // sort contour in x/y directions to easily find min/max and next
    std::vector<cv::Point> cSortedX = contours.at(id);
    std::sort(cSortedX.begin(), cSortedX.end(), sortX);

    std::vector<cv::Point> cSortedY = contours.at(id);
    std::sort(cSortedY.begin(), cSortedY.end(), sortY);


    unsigned int minXId = 0;
    unsigned long maxXId = cSortedX.size() - 1;

    unsigned int minYId = 0;
    unsigned long maxYId = cSortedY.size() - 1;

    cv::Rect interiorBB;

    while ((minXId < maxXId) && (minYId < maxYId)) {
        cv::Point min(cSortedX[minXId].x, cSortedY[minYId].y);
        cv::Point max(cSortedX[maxXId].x, cSortedY[maxYId].y);

        interiorBB = cv::Rect(min.x, min.y, max.x - min.x, max.y - min.y);

        // out-codes: if one of them is set, the rectangle size has to be reduced at that border
        int ocTop = 0;
        int ocBottom = 0;
        int ocLeft = 0;
        int ocRight = 0;

        bool finished = checkInteriorExterior(contourMask, interiorBB, ocTop, ocBottom, ocLeft,
                                              ocRight);
        if (finished) {
            break;
        }

        // reduce rectangle at border if necessary
        if (ocLeft)++minXId;
        if (ocRight) --maxXId;

        if (ocTop) ++minYId;
        if (ocBottom)--maxYId;
    }
    if (interiorBB.empty() && interiorBB.br().x <= 10 && interiorBB.br().y <= 10 &&
        interiorBB.width <= 20 && interiorBB.height <= 20) {
        return status;
    }
    cv::Point tl = interiorBB.tl();
    cv::Point tl1 = cvPoint(tl.x + 10, tl.y + 10);
    cv::Point br = interiorBB.br();
    cv::Point br1 = cvPoint(br.x - 10, br.y - 10);
    cv::Rect newRect(tl1, br1);
    cv::Mat image_roi = input(newRect);
    imwrite(saveImagePath, image_roi);
    return status;
}

void
CVWrapper::createGuangijaoPano(int imageNum, const char **imagePaths, const char *saveImagePath,
                               const char *createPano, float retry) {
    vector<Mat> imgs;
    Mat img;
    LOGD("开始");

    for (int i = 0; i < imageNum; i++) {
        string pic_path = imagePaths[i];
        img = imread(pic_path, 1);
        if (!img.rows) {
            continue;
        }
        Mat smallImag;
        Size sz = img.size();
        float rate = retry;//原图压缩比例
        sz.height = sz.height / rate;
        sz.width = sz.width / rate;
        resize(img, smallImag, sz, 0, 0, 3);
        imgs.push_back(smallImag);
    }

    Mat pano;
    Rect pano_size;
    Stitcher::Status status = genCutpano(imgs, pano, pano_size, saveImagePath);


    LOGD("结束合成全景......");
}

void CVWrapper::fanzhuan(const char *sImagePath, const char *saveImagePath) {
    cv::Mat sImage = cv::imread(sImagePath);
    if (!sImage.rows) {
        return;
    }

    cv::Mat result;
    flip(sImage, result, -1);
    if (!result.rows) {
        return;
    }

    cv::imwrite(saveImagePath, result);

    LOGD("翻转结束......");
}


void CVWrapper::compressedImg(const char *inputUrl, const char *outputUrl) {
    cv::Mat sImage = cv::imread(inputUrl, IMREAD_REDUCED_COLOR_2);
    if (!sImage.rows) {
        return;
    }
    LOGD("开始压缩图片......");
    Mat smallImag;
    Size sz = sImage.size();
    sz.height = 1080;
    sz.width = 1920;
    resize(sImage, smallImag, sz, 0, 0, 3);
    cv::imwrite(outputUrl, smallImag);
}

void CVWrapper::createPano(int imageNum, const char **imagePaths, const char *saveImagePath,
                           const char *createPath, float retry) {
    vector<Mat> imgs;
    Mat img;
    LOGD("开始合成全景......");
#ifdef HAVE_OPENCL
    LOGD("开启了OpenCLient");
#endif
    LOGD("开始");
    for (int i = 0; i < imageNum; i++) {
        string pic_path = imagePaths[i];
        img = imread(pic_path, 1);
        if (!img.rows) {
            continue;
        }
        LOGD("开始啦");
        Mat smallImag;
        Size sz = img.size();
        float rate = retry;//原图压缩比例
        sz.height = sz.height / rate;
        sz.width = sz.width / rate;
        resize(img, smallImag, sz, 0, 0, 3);
        imgs.push_back(smallImag);
    }

    Mat pano;
    Rect pano_size;
    Stitcher::Status status = genpano(imgs, pano, pano_size, saveImagePath);
    if (status == Stitcher::Status::ERR_NEED_MORE_IMGS) {
        printf("stitching failed !\r\n");
        if (errorCallback != nullptr) {
            errorCallback(-1);
        }
    } else if (status == Stitcher::Status::ERR_CAMERA_PARAMS_ADJUST_FAIL) {
        LOGD("出错了");
        if (errorCallback != nullptr) {
            errorCallback(-1);
        }
    } else {
        LOGD("合成成功");
//        pano = imread(saveImagePath,1);
        if (errorCallback != nullptr) {
            errorCallback(0);
        }
    }

    LOGD("结束合成全景......");
}

void CVWrapper::addPanoSky(const char *pPath, const char *sPath, float sHeight, int baohedu,
                           int duibidu, int liangdu,
                           const char *saPath) {
    LOGD("全景图片合成天空");
    Mat pImage = imread(pPath, IMREAD_COLOR);
    if (!pImage.rows) {
        LOGD("全景图片没有读入内存");
    }
    Mat sImage = imread(sPath, IMREAD_UNCHANGED);
    if (!sImage.rows) {
        LOGD("天空没有读入内存");
    }

    Ptr<Stitcher> stitcher = Stitcher::create(Stitcher::PANORAMA, false);
    cv::Mat resut1, result2, result;
    stitcher->adjustSaturation(pImage, baohedu, result);
    stitcher->adjustContrast(result, duibidu, result);
    stitcher->adjustBrightness(result, liangdu, result);


    Size newSImageSize = sImage.size();
    newSImageSize.width = result.cols;
    LOGD("-----》" + result.rows);
    newSImageSize.height = (int) (result.rows * sHeight);

    Mat newSImage;
    resize(sImage, newSImage, newSImageSize);
    LOGD("-----》" + newSImage.rows);
    Mat mat_sky2bk(result, Rect(0, 0, newSImage.cols, newSImage.rows));
    std::vector<cv::Mat> scr_channels;
    std::vector<cv::Mat> dstt_channels;
    split(newSImage, scr_channels);
    split(mat_sky2bk, dstt_channels);
    for (int i = 0; i < 3; i++) {
        dstt_channels[i] = dstt_channels[i].mul(255.0 - scr_channels[3], 1 / 255.0);
        dstt_channels[i] += scr_channels[i].mul(scr_channels[3], 1 / 255.0);
    }
    merge(dstt_channels, mat_sky2bk);
    imwrite(saPath, result);
}


void CVWrapper::cutImage(float x, float y, float w, float h, float screenWith, float screenheight,
                         const char *srcPath, const char *toPath,
                         float scale) {
    cv::Mat sImage = cv::imread(srcPath);
    if (!sImage.rows) {
        return;
    }

    cv::Size newSImageSize = sImage.size();
    if (scale < 1.0f) {
        scale = 1.0f;
    }
    if (scale > 4.0f) {
        scale = 4.0f;
    }

    newSImageSize.width = (int) (sImage.cols * scale);
    newSImageSize.height = (int) (sImage.rows * scale);
    cv::Mat newSImage;
    resize(sImage, newSImage, newSImageSize);

    int x1 = (int) (x / screenWith * sImage.size().width);
    int y1 = (int) (y / screenheight * sImage.size().height);
    int w1 = (int) (w / screenWith * sImage.size().width);
    int h1 = (int) (h / screenheight * sImage.size().height);

    cv::Rect cutRect(x1, y1, w1, h1);
    cv::Mat image_roi = newSImage(cutRect);

    cv::imwrite(toPath, image_roi);
}

Mat CVWrapper::editPano(cv::Mat sImage, int baohedu, int duibidu, int liangdu) {
    if (!sImage.rows) {
        return Mat();
    }
    Ptr<Stitcher> stitcher = Stitcher::create(Stitcher::PANORAMA, false);
    cv::Mat resut1, result2, result, dst;
    stitcher->adjustSaturation(sImage, baohedu, result);
    stitcher->adjustContrast(result, duibidu, result);
    stitcher->adjustBrightness(result, liangdu, result);
//    cvtColor(result, dst, CV_BGR2RGB);
    return result;
}

void CVWrapper::editPano(const char *sPath, int baohedu, int duibidu, int liangdu,
                         const char *dPath) {
    cv::Mat sImage = cv::imread(sPath);
    if (!sImage.rows) {
        return;
    }
    Ptr<Stitcher> stitcher = Stitcher::create(Stitcher::PANORAMA, false);
    cv::Mat resut1, result2, result;
    stitcher->adjustSaturation(sImage, baohedu, resut1);
    stitcher->adjustContrast(resut1, duibidu, result2);
    stitcher->adjustBrightness(result2, liangdu, result);

    cv::imwrite(dPath, result);
}

