package com.feipai.flypai.utils.global;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;

/**
 * SD卡相关工具类
 */
public class SDCardUtils {

    private SDCardUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 判断SD卡是否可用
     *
     * @return true : 可用<br>false : 不可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SD卡路径
     *
     * @return SD卡路径
     */
    public static String getSDCardPath() {
        if (!isSDCardEnable()) return "";
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    /**
     * 获取SD卡data路径
     *
     * @return SD卡data路径
     */
    public static String getDataPath() {
        if (!isSDCardEnable()) return "";
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "datas" + File.separator;
    }

    /**
     * 获取SD卡剩余空间
     *
     * @return SD卡剩余空间
     */
    public static String getFreeSpace() {
        if (!isSDCardEnable()) return "";
        try {
            final StatFs statFs = new StatFs(getSDCardPath());
            long blockSize = 0;
            long availableBlocks = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                availableBlocks = statFs.getAvailableBlocksLong();
            } else {
                blockSize = statFs.getBlockSize();
                availableBlocks = statFs.getAvailableBlocks();
            }
            return Formatter.formatFileSize(Utils.context, blockSize * availableBlocks);
        } catch (Error e) {
            return "获取空间失败";
        } catch (Exception e) {
            return "获取空间失败";
        }
    }

    /**
     * 获取SD卡剩余空间大小
     *
     * @return SD卡剩余空间
     */
    public static long getFreeSpaceSize() {
        if (!isSDCardEnable()) return 0;
        try {
            final StatFs statFs = new StatFs(getSDCardPath());
            long blockSize = 0;
            long availableBlocks = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                availableBlocks = statFs.getAvailableBlocksLong();
            } else {
                blockSize = statFs.getBlockSize();
                availableBlocks = statFs.getAvailableBlocks();
            }
            return blockSize * availableBlocks;
        } catch (Error e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取总大小
     */
    public static String getSDTotalSize() {
        if (!isSDCardEnable()) return "";
        try {
            final StatFs statFs = new StatFs(getSDCardPath());
            long blockSize = 0;
            long blockCountLong = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                blockCountLong = statFs.getBlockCountLong();
            } else {
                blockSize = statFs.getBlockSize();
                blockCountLong = statFs.getBlockCount();
            }
            return Formatter.formatFileSize(Utils.context, blockSize * blockCountLong);
        } catch (Exception e) {
            return "获取空间失败";
        } catch (Error e) {
            return "获取空间失败";
        }
    }

    /**
     * 获取SD卡信息
     *
     * @return SDCardInfo
     */
    public static String getSDCardInfo() {
        SDCardInfo sd = new SDCardInfo();
        if (!isSDCardEnable()) return "";
        sd.isExist = true;
        StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sd.totalBlocks = sf.getBlockCountLong();
            sd.blockByteSize = sf.getBlockSizeLong();
            sd.availableBlocks = sf.getAvailableBlocksLong();
            sd.availableBytes = sf.getAvailableBytes();
            sd.freeBlocks = sf.getFreeBlocksLong();
            sd.freeBytes = sf.getFreeBytes();
            sd.totalBytes = sf.getTotalBytes();
        } else {
            sd.totalBlocks = sf.getBlockCount();
            sd.blockByteSize = sf.getBlockSize();
            sd.availableBlocks = sf.getAvailableBlocks();
            sd.availableBytes = sf.getBlockSize() * sf.getAvailableBlocks();
            sd.freeBlocks = sf.getFreeBlocks();
            sd.freeBytes = sf.getBlockSize() * sf.getAvailableBlocks();
            sd.totalBytes = sf.getBlockSize() * sf.getBlockCount();
        }
        return sd.toString();
    }

    public static class SDCardInfo {
        boolean isExist;
        long totalBlocks;
        long freeBlocks;
        long availableBlocks;
        long blockByteSize;
        long totalBytes;
        long freeBytes;
        long availableBytes;

        @Override
        public String toString() {
            return "isExist=" + isExist +
                    "\ntotalBlocks=" + totalBlocks +
                    "\nfreeBlocks=" + freeBlocks +
                    "\navailableBlocks=" + availableBlocks +
                    "\nblockByteSize=" + blockByteSize +
                    "\ntotalBytes=" + totalBytes +
                    "\nfreeBytes=" + freeBytes +
                    "\navailableBytes=" + availableBytes;
        }
    }
}