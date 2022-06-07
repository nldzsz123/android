package opencv;

import com.feipai.flypai.api.RxLoopObserver;
import com.feipai.flypai.api.RxLoopSchedulers;
import com.feipai.flypai.mvp.BaseView;
import com.feipai.flypai.utils.global.FileUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.flypie.videostab.Videostab;
import com.videoplayer.NativeCode;

import java.io.File;

import io.reactivex.functions.Function;

import static com.feipai.flypai.app.ConstantFields.INTENT_PARAM.ADVERTISING_4KAIR;

public class OpencvLib {
    static {
        System.loadLibrary("panoLib");
    }

    private ProgressCallBack mCallBack;
    private BaseView mBaseView;

    private String[] imgUrls;
    private String savePath;
    private String createPath;
    private float retryCount;

    private int saturation;
    private int contrast;
    private int brightness;

    public OpencvLib(BaseView baseView, ProgressCallBack callBack) {
        this.mCallBack = callBack;
        this.mBaseView = baseView;
    }


    /**
     * 合成全景 or 广角
     * images:图片的绝对路径
     * savePath:合成的全景保存的绝对路径
     * return 0 合成成功 -1 合成失败或者合成的过程中出现错误
     * note:1、合成全景的保存路径要检查有没有写入权限 2、该函数是阻塞当前线程的直到函数返回
     */
    public void createPanorama(String[] imgUrls, String savePath, String createPath, float retryCount) {
        for (String path : imgUrls) {
            LogUtils.d("合成全景的图片路径-" + path);
        }
        LogUtils.d("合成全景的图片保存路径-" + savePath);
        RxLoopSchedulers.composeIO(mBaseView, new Function() {
            @Override
            public Object apply(Object object) throws Exception {
                if (imgUrls.length < 14) {
                    createGuangjiaoPanoramaNative(imgUrls.length, imgUrls, savePath, createPath, retryCount);
                } else {
                    createPanoramaNative(imgUrls.length, imgUrls, savePath, createPath, retryCount);
                }
                return savePath;
            }
        }).subscribe(new RxLoopObserver() {
            @Override
            public void onNext(Object o) {
                super.onNext(o);
                this.disposeDisposables();

            }
        });
    }



    public void picsToVideo(String[] imgUrls, String savePath, int delayResolution) {
        this.savePath = savePath;
        LogUtils.d("合成延时的分辨率====>" + delayResolution);
        RxLoopSchedulers.composeIO(mBaseView, new Function() {
            @Override
            public String apply(Object object) throws Exception {
                Videostab.picsToVideo(imgUrls, imgUrls.length, savePath, delayResolution == ADVERTISING_4KAIR ? 540 : 720);
                LogUtils.d("保存路径====>" + savePath);
                String tagPath = savePath.replace("avi", "MP4");
                NativeCode.aviToMp4(savePath, tagPath);
                return savePath;
            }
        }).subscribe(new RxLoopObserver<String>() {
            @Override
            public void onNext(String path) {
                super.onNext(path);
                this.disposeDisposables();
                if (mCallBack != null) {
                    mCallBack.picToAviSuccesse(path);
                }
            }
        });

    }


    /**
     * 全景圖片调节器
     */
    public void adjustPanoramaToMemory(int[] bmpPxs, int h, int w, int saturation, int contrast, int brightness) {
        this.saturation = saturation;
        this.contrast = contrast;
        this.brightness = brightness;
//        MLog.log("饱和度=" + saturation + "对比度=" + contrast + "亮度=" + brightness);
        RxLoopSchedulers.composeIO(mBaseView, new Function() {
            @Override
            public Object apply(Object object) throws Exception {
                int[] resultInt = regulationPanoramaToMemory(bmpPxs, w, h, saturation, contrast, brightness);
                return resultInt;
            }
        }).subscribe(new RxLoopObserver() {
            @Override
            public void onNext(Object o) {
                super.onNext(o);
                this.disposeDisposables();
                if (mCallBack != null) {
                    mCallBack.regulationResult((int[]) o);
                }
            }
        });
    }

    public void rotatingPanorImg(String panoPath) {
        rotatingPanorImgNative(panoPath);
    }


    /**
     * 合成广角
     */
    private native void createGuangjiaoPanoramaNative(int argc, String[] argv, String path, String retry, float retryCount);

    /**
     * 合成全景
     */
    private native void createPanoramaNative(int argc, String[] argv, String path, String retry, float retryCount);


    //调节全景图保存成文件
    private native void regulationPanoramaToFile(String panorPath, int saturation, int contrast, int brightness, String savePath);

    /**
     * 图片翻转
     */
    private native void fanzhuan(String sPath, String savePath);

    //调节全景图保存到内存中
    private native int[] regulationPanoramaToMemory(int[] bmpArray, int w, int h, int saturation, int contrast, int brightness);


    /**
     * 全景添加天空
     */
    private native void addPanoramaSkyNative(String panoPath, String skyPath, String savePath, float hight, int baohedu, int duibidu, int liangdu);

    /**
     * 图片旋转
     */
    private native void rotatingPanorImgNative(String panoPath);

    /**
     * 对频
     */
    public native static boolean duipingNative(String ssid);

    /**
     * 图片压缩
     */
    public native static String compressedImgNative(String url, String outputPath);


    private void updateProgress(int progress) {
        if (mCallBack != null) {
            mCallBack.progress(progress);
        }
    }

    private void result(int errorCode) {
        if (mCallBack != null) {
            mCallBack.result(errorCode);
        }
    }

    public interface ProgressCallBack {
        // 合成进度 range 0-100.当进度到100时 也就合成完成了
        void progress(int progress);

        // 合成过程中发生错误时调用，一旦发生错误，合成将终止
        // errorCode 0代表没有合成成功  -1 代表合成发生错误了
        // note 当合成进度为100时 此回调也会调用一次 此时errorCode为0,其它情况调用时则为合成发生错误了
        void result(int errorCode);

        // 调节生成bitmap回调
        void regulationResult(int[] bitmap);

        void picToAviSuccesse(String url);

        void addSkyToPicSuccess(boolean isScuccess);
    }

}
