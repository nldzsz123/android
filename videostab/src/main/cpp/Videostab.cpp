#include "VideoStab.h"
#include "opencv2/video.hpp"
#include "opencv2/videoio.hpp"
#include "opencv2/videostab.hpp"
#include "opencv2/imgcodecs.hpp"
#include <android/log.h>

#define LOG_PRINT(level,fmt,...) __android_log_print(level,"VideoStab",fmt,##__VA_ARGS__)

#if 1
#define LOGD(fmt,...) LOG_PRINT(ANDROID_LOG_DEBUG,fmt ,##__VA_ARGS__)
#else
#define LOGD(...) LOG_NOOP
#endif

using namespace std;
using namespace cv;
using namespace cv::detail;

// 视频稳定输出
void videoOutput(Ptr<IFrameSource> stabFrames, string outputPath)
{
    VideoWriter writer;
    cv::Mat stabFrame;
    int nframes = 0;
    // 设置输出帧率
    double outputFps = 24;
    // 遍历搜索视频帧
    while (!(stabFrame = stabFrames->nextFrame()).empty()){
        nframes++;
        // 输出视频稳定帧
        if (!outputPath.empty()){
            if (!writer.isOpened()){
                writer.open(outputPath, VideoWriter::fourcc('M', 'J', 'P', 'G'),
                            outputFps, stabFrame.size());
            }

            writer << stabFrame;
        }
    }
    LOGD("nFrames: %d",nframes);
    LOGD("finished ");
}

void cacStabVideo(Ptr<IFrameSource> stabFrames, string inputPath,string outputPath)
{
    try
    {
        Ptr<VideoFileSource> srcVideo = makePtr<VideoFileSource>(inputPath);
        LOGD("frame count: %d",srcVideo->count());
        LOGD("frame w: %d",srcVideo->width());
        LOGD("frame h: %d",srcVideo->height());
        LOGD("frame fps: %.0f",srcVideo->fps());

        // 运动估计
        double estPara = 0.1;
        Ptr<MotionEstimatorRansacL2> est = makePtr<MotionEstimatorRansacL2>(MM_HOMOGRAPHY);

        // Ransac参数设置
        RansacParams ransac = est->ransacParams();
        ransac.size = 30;
        ransac.thresh = 5;
        ransac.eps = 0.5;   // 无效数据所占的比例
        int radius_pass = 15;

        // Ransac计算
        est->setRansacParams(ransac);
        est->setMinInlierRatio(estPara);    // 有效数据的比例

        // Fast特征检测
        Ptr<FastFeatureDetector> feature_detector = FastFeatureDetector::create();

        // 运动估计关键点匹配
        Ptr<KeypointBasedMotionEstimator> motionEstBuilder = makePtr<KeypointBasedMotionEstimator>(est);

        // 设置特征检测器
        motionEstBuilder->setDetector(feature_detector);
        Ptr<IOutlierRejector> outlierRejector = makePtr<NullOutlierRejector>();
        motionEstBuilder->setOutlierRejector(outlierRejector);

        // 3-Prepare the stabilizer
        StabilizerBase *stabilizer = 0;
        // with a two pass stabilizer
        bool est_trim = true;
        TwoPassStabilizer *twoPassStabilizer = new TwoPassStabilizer();
        twoPassStabilizer->setEstimateTrimRatio(est_trim);
        twoPassStabilizer->setMotionStabilizer(makePtr<GaussianMotionFilter>(radius_pass));
        stabilizer = twoPassStabilizer;

        // second, set up the parameters
        bool incl_constr = true;
        stabilizer->setFrameSource(srcVideo);
        stabilizer->setMotionEstimator(motionEstBuilder);
        stabilizer->setRadius(radius_pass);
        stabilizer->setTrimRatio(0.1);
        stabilizer->setCorrectionForInclusion(incl_constr);
        stabilizer->setBorderMode(BORDER_REPLICATE);
        // cast stabilizer to simple frame source interface to read stabilized frames
        stabFrames.reset(dynamic_cast<IFrameSource*>(stabilizer));
        // 4-videoOutput the stabilized frames. The results are showed and saved.

        LOGD("开始写入视频......");
        videoOutput(stabFrames, outputPath);
    }
    catch (const exception &e)
    {
        cout << "error: " << e.what() << endl;
        stabFrames.release();
    }
}