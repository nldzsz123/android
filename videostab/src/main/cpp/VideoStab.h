//
//  VideoStab.hpp
//  study
//
//  Created by 飞拍科技 on 2019/3/8.
//  Copyright © 2019 飞拍科技. All rights reserved.
//

#ifndef VideoStab_hpp
#define VideoStab_hpp

#include<opencv2/videostab.hpp>
#include<string>
#include<iostream>

using namespace std;
using namespace cv;
using namespace cv::videostab;

#include <stdio.h>
/**
 *  Undefined symbols for architecture arm64:
 *  "_OBJC_CLASS_$_ALAssetsLibrary", referenced from:
 *  objc-class-ref in opencv2(cap_ios_video_camera.o)
 *  导入CoreVideo.framework，CoreMedia.framework，AssetsLibrary.framework三个库即不会出错了
 */
void cacStabVideo(Ptr<IFrameSource> stabFrames, string inputPath,string outputPath);

#endif /* VideoStab_hpp */
