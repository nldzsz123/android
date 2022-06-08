package com.feipai.flypai.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipai.flypai.app.ConstantFields;
import com.feipai.flypai.base.BaseEntity;
import com.feipai.flypai.utils.cache.CacheManager;
import com.feipai.flypai.utils.global.FileUtils;
import com.feipai.flypai.utils.global.LogUtils;
import com.feipai.flypai.utils.global.TimeUtils;
import com.videoplayer.NativeCode;
import com.videoplayer.VideoClient;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileBean extends BaseEntity implements MultiItemEntity {

    public String name;
    /**
     * 文件类型
     * MultiItemEntity
     * 1视频
     * 2全景文件
     * 3全景文件夹
     * 4广角文件夹
     * 5延时视频
     */
    public int type;

    /**
     * 时间
     */
    public long time = 0;

    /**
     * 大小
     */
    public long fileSize;

    /**
     * 本地根路径
     */
    public String locPath;


    /**
     * 本地绝对路径
     */
    public String locAbsolutePath;
    /**
     * 远程绝对路径
     */
    public String remoteAbsolutePath;

    /**
     * 远程根路径
     */
    public String remotePath;

    /**
     * 视频时长
     */
    public long videoDuration;

    /**
     * 视频分辨率
     */
    public String videoReselution;

    /**
     * 全景，延时或者广角文件组
     */
    public FileBean[] fbs;


    public byte[] bmps;


    /**
     * 是否已下载
     */
    public boolean isDown;

    /**
     * 是否已合成
     */
    public boolean isCompound;


    /**
     * 是否被选中
     */
    public boolean isSelected;


    /**
     * 远程缩略图读取的长度
     */
    public int readBitmapLength = 0;


    /**
     * is only for download from socket
     */

    public boolean isDownloading;

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    /**
     * 月日组合
     * * 格式化日期
     * * 04月10日
     */
    public String getMotherDate() {
        String motherTime = TimeUtils.getScoreDates(time);
        return motherTime;
    }

    /**
     * 时间组合
     * 格式化日期
     * 16:04:03
     */
    public String getTimeHHmmss() {
        String dateTime = TimeUtils.millis2String(time, "HH:mm:ss");
        return dateTime;
    }

    public String getTimeHHmm() {
        String dateTime = TimeUtils.millis2String(time, "HH:mm");
        return dateTime;
    }

    /**
     * "0 bytes|2019-04-01 10:59:04"
     */
    public void setStringTime(String time) {
        if (time.indexOf("b") - 1 > 0) {
            setFileSize(Long.parseLong(time.substring(0, time.indexOf("b") - 1)));
        }
        if (time.length() > (time.indexOf("|") + 1)) {
            time = time.substring(time.indexOf("|") + 1, time.length());
            Date date = TimeUtils.string2Date(time, "yyyy-MM-dd HH:mm:ss");
            this.time = TimeUtils.date2Millis(date);
        }
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    public String getLocPath() {
        return locPath;
    }

    public void setLocPath(String locPath) {
        this.locPath = locPath;
    }

    public String getLocAbsolutePath() {
        return locAbsolutePath;
    }

    public void setLocAbsolutePath(String locAbsolutePath) {
        this.locAbsolutePath = locAbsolutePath;
    }

    public String getRemoteAbsolutePath() {
        return remoteAbsolutePath;
    }

    public void setRemoteAbsolutePath(String remoteAbsolutePath) {
        switch (type) {//只针对远程文件做处理
            case ConstantFields.FILE_TYPE.TYPE_PHOTO:
            case ConstantFields.FILE_TYPE.TYPE_VIDEO:
                setLocPath(FileUtils.createDirPath(ConstantFields.SD_DIR.DIR));
                setLocAbsolutePath(getLocPath() + File.separator + name);
                break;
        }
        this.remoteAbsolutePath = remoteAbsolutePath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getVideoDuration() {
        return TimeUtils.timeParse(videoDuration);
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getVideoReselution() {
        return videoReselution;
    }

    public void setVideoReselution(String videoReselution) {
        this.videoReselution = videoReselution;
    }

    public FileBean[] getFbs() {
        return fbs;
    }

    public void setFbs(FileBean[] fbs) {
//        long fbsSize = fileSize;
        if (remotePath != null) {//只针对远程文件做处理
            if (fbs != null && fbs.length > 0) {
                for (int i = 0; i < fbs.length; i++) {
                    FileBean bean = fbs[i];
//                    fbsSize += bean.getFileSize();
                    if (i == 0) {
                        switch (type) {
                            case ConstantFields.FILE_TYPE.TYPE_DELAY_DIR:
                                setLocPath(FileUtils.createDirPath(ConstantFields.SD_DIR.DIR)
                                        + File.separator + ConstantFields.SD_DIR.TIME
                                        + bean.getName().substring(0, bean.getName().lastIndexOf(".")));
                                setLocAbsolutePath(getLocPath() + File.separator + bean.getName());
                                break;
                            case ConstantFields.FILE_TYPE.TYPE_PANOR_DIR:
                                setLocPath(FileUtils.createDirPath(ConstantFields.SD_DIR.DIR) +
                                        File.separator + ConstantFields.SD_DIR.PANOR
                                        + bean.getName().substring(0, bean.getName().lastIndexOf("."))
                                        + ConstantFields.SD_DIR.VR);
                                setLocAbsolutePath(getLocPath() + File.separator + bean.getName());
                                break;
                        }
                        File file = new File(getLocPath());
                        if (file != null && FileUtils.isFileExists(file) && FileUtils.isDir(file)) {
                            List<File> files = FileUtils.listFilesInDir(file);
                            setDown(files != null && files.size() == fbs.length);
                        }
                    }
                    bean.setDown(isDown());
                    bean.setLocPath(getLocPath());
                    bean.setLocAbsolutePath(getLocPath() + File.separator + bean.getName());
                }
//                setFileSize(fbsSize);
            }
        }
        this.fbs = fbs;
//        if (fbs != null) LogUtils.d("文件夹中文件个数===>名称=" + name + "，类型" + type + ",个数=" + fbs.length);
    }

    public boolean isDown() {
        if (isDir()) {
        } else {
            File file = FileUtils.getFileByPath(locAbsolutePath);
            isDown = FileUtils.isFileExists(file);
            if (isDown) {
                isDown = file.length() == fileSize;
                if (!isDown) {
                    FileUtils.deleteFile(file);
                }
            }
        }
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public boolean isCompound() {
        return isCompound;
    }

    public void setCompound(boolean compound) {
        isCompound = compound;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getReadBitmapLength() {
        return readBitmapLength;
    }

    public void setReadBitmapLength(int readBitmapLength) {
        this.readBitmapLength = readBitmapLength;
    }

//    public byte[] getBmps() {
//        return CacheManager.getACache().getAsBinary(name+".JPG");
//    }

    public Bitmap getBmp() {
        if (isDir())
            return CacheManager.getACache().getAsBitmap(locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")));
        return CacheManager.getACache().getAsBitmap(name);
    }

    public File getBmpCacheFile() {
        if (isDir()) {
            return CacheManager.getACache().file(locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")));
        }
        return CacheManager.getACache().file(name);
    }


    public void setBmps(byte[] bys, int w) {
        LogUtils.d("保存缩略图====>" + toString() + "||" + w);
        if (type == ConstantFields.FILE_TYPE.TYPE_VIDEO) {
            CacheManager.getACache().put(name, VideoClient.confertFromIdr(bys, 0.2f));
        } else {
            if (isDir()) {
                LogUtils.d("文件夹的缩略图====>" + locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")));
                CacheManager.getACache().put(locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")), bys);
            } else {
                CacheManager.getACache().put(name, bys);
            }
        }
        this.bmps = bys;
    }

    public File getBigThumbFile() {
        if (isDir()) {
            return CacheManager.getACache().file(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START +locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")));
        }
        return CacheManager.getACache().file(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + name);
    }


    public void setBigThumb(byte[] bys, int w) {
        if (bys != null && bys.length > 0) {
            if (type == ConstantFields.FILE_TYPE.TYPE_VIDEO) {
                CacheManager.getACache().put(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + name, VideoClient.confertFromIdr(bys, 0.2f));
            } else {
                if (isDir()) {
                    LogUtils.d("保存缩略图大====>" + w);
                    CacheManager.getACache().put(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + locAbsolutePath.substring(locAbsolutePath.lastIndexOf("/")), bys);
                } else {
                    CacheManager.getACache().put(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + name, bys);
                }
            }
        }
        this.bmps = bys;
    }

    public void clearThumb() {
        if (isDir()) {
            CacheManager.getACache().remove(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + fbs[0].getName());
            CacheManager.getACache().remove(fbs[0].getName());
            CacheManager.getACache().remove(FileUtils.getFileNameNoExtension(fbs[0].getName()));
        } else {
            CacheManager.getACache().remove(ConstantFields.APP_CONFIG.CACHE_BIG_THUMB_NAME_START + name);
            CacheManager.getACache().remove(name);
            CacheManager.getACache().remove(FileUtils.getFileNameNoExtension(name));
        }
    }


    /**
     * 文件夹组
     */
    public boolean isDir() {
        return type == ConstantFields.FILE_TYPE.TYPE_DELAY_DIR || type == ConstantFields.FILE_TYPE.TYPE_PANOR_DIR;
    }

    public boolean equals(Object obj) {
        if (obj instanceof FileBean) {
            FileBean fb = (FileBean) obj;
            if (fb.isDir()) {
                if (locPath == null || fb.locPath == null) {
                    return name.equals(fb.name);
                }
                return locPath.equals(fb.locPath) && locAbsolutePath.equals(fb.locAbsolutePath);
            } else {
//                LogUtils.d("比较" + name + "|||||" + fileSize + "======>" + fb.name + "||||" + fb.fileSize);
                return this.name.equals(fb.name) && this.type == fb.type;
            }
        }
        return super.equals(obj);
    }


    @Override
    public String toString() {
        return "FileBean{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", time=" + time +
                ", fileSize=" + fileSize +
                ", locPath='" + locPath + '\'' +
                ", locAbsolutePath='" + locAbsolutePath + '\'' +
                ", remoteAbsolutePath='" + remoteAbsolutePath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", videoDuration=" + videoDuration +
                ", videoReselution='" + videoReselution + '\'' +
                ", fb的大小='" + (fbs == null ? 0 : fbs.length) + '\'' +
//                ", fbs=" + Arrays.toString(fbs) +
//                ", bmps=" + Arrays.toString(bmps) +
                ", isDown=" + isDown +
                ", isCompound=" + isCompound +
                ", isSelected=" + isSelected +
                ", readBitmapLength=" + readBitmapLength +
                '}';
    }

    @Override
    public int getItemType() {
        return getType();
    }
}
