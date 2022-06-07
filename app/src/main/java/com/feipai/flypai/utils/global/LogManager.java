package com.feipai.flypai.utils.global;

import android.text.TextUtils;

import com.feipai.flypai.app.ConstantFields;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogManager {

    // 日志名称
    private static final String LOG_HEADER_FORMAT = "--> %s -- [Log]  ";
    // 日志文件名
    private static final String LOG_FILE_NAME_FORMAT = "%s.txt";
    // 日志文件名日期格式
    private static final String LOG_FILE_NAME_DATE_FORMAT = "yyyy-MM-dd";
//    // 日志目录
    private String LOG_CACHE_DIR = ConstantFields.APP_CONFIG.LOG_CACHE_DIR_PATH;
    // 日志合并文件名称
    private static final String LOG_MERGE_FILE_NAME = "merge.txt";
    // 保存日志的天数
    private static final int SAVE_LOG_DAYS = 7;

    // 单例对象
    private static LogManager mInstance;
    // 日志队列
    private LinkedBlockingQueue<String> mLogQueue = new LinkedBlockingQueue<>();
    // 消费者线程是否在运行
    private AtomicBoolean isLogWriterRunning = new AtomicBoolean(false);
    // 时间格式
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    // 日志写入器
    private LogWriter mLogWriter = new LogWriter();
    // 日志文件名称
    private File mLogFile;
    // 当前日期
    private String mCurrentDate;

    private LogManager() {
        // 检测删除过期日志
        checkDeletePastDueLog();
    }

    /**
     * 获取实例对象
     */
    public static LogManager getInstance() {
        if (mInstance == null) {
            synchronized (LogManager.class) {
                if (mInstance == null) {
                    mInstance = new LogManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化日志文件名
     */
    public void init() {
        SimpleDateFormat format = new SimpleDateFormat(LOG_FILE_NAME_DATE_FORMAT, Locale.getDefault());
        mCurrentDate = format.format(Calendar.getInstance().getTime());
        String logFileName = String.format(LOG_FILE_NAME_FORMAT, mCurrentDate);
        if (FileUtils.getSdPaths(LOG_CACHE_DIR)!=null)
            mLogFile = new File(FileUtils.getSdPaths(LOG_CACHE_DIR), logFileName);
    }

    /**
     * 停止打印日志
     */
    public void stopLogWriterRunning() {
        isLogWriterRunning.set(false);
    }

    /**
     * 打印日志
     */
    public void print(String msg) {
        print(msg, null);
    }

    /**
     * 打印日志
     */
    public void print(Throwable e) {
        print("", e);
    }

    /**
     * 打印日志
     */
    public void print(String msg, Throwable e) {
        String logMsg;
        if (TextUtils.isEmpty(msg) && e == null) {
            return;
        } else if (TextUtils.isEmpty(msg) && e != null) {
            logMsg = e.toString();
        } else if (!TextUtils.isEmpty(msg) && e != null) {
            logMsg = msg + "," + e.toString();
        } else {
            logMsg = msg;
        }
//        Logger.d(logMsg);
        // 拼接写入文件的时间日志
        String logHeader = String.format(LOG_HEADER_FORMAT, mSimpleDateFormat.format(Calendar.getInstance().getTime()));
        try {
            mLogQueue.put(logHeader + logMsg + "\n");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // 确保运行着
        mLogWriter.ensureRunning();
    }

    /**
     * 获取日志文件
     *
     * @return 如果日志文件不为空返回文件路径，反之返回null
     */
    public String getLogFileByDate(String date) {
        File logDir = new File(FileUtils.getSdPaths(LOG_CACHE_DIR));
        if (!logDir.exists() || !CollectionUtils.isNotNullOrEmptyArray(logDir.listFiles())) {
            return null;
        }
        List<File> logFiles = new ArrayList<>();
        for (File logFile : logDir.listFiles()) {
            if (logFile.getName().startsWith(date)) {
                logFiles.add(logFile);
            }
        }
        // 检测是否有当天日志文件
        if (CollectionUtils.isNullOrEmpty(logFiles)) {
            return null;
        }
        // 合并日志文件
        File mergeFile = new File(logDir, LOG_MERGE_FILE_NAME);
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        for (File logFile : logFiles) {
            FileUtils.writeFileFromString(mergeFile, "-------------------" + logFile.getName() + "\n\n", true);
            FileUtils.writeFileFromString(mergeFile, FileUtils.readFile2String(logFile, "UTF-8"), true);
            FileUtils.writeFileFromString(mergeFile, "\n\n\n\n\n\n", true);
        }
        return mergeFile.getAbsolutePath();
    }

    /**
     * 检测删除过期日志，只保存近7天的日志
     */
    public void checkDeletePastDueLog() {
        File logDir = new File(FileUtils.getSdPaths(LOG_CACHE_DIR));
        if (!logDir.exists() || !CollectionUtils.isNotNullOrEmptyArray(logDir.listFiles())) {
            return;
        }
        for (File logFile : logDir.listFiles()) {
            if (logFile.exists() && isPastDueLogFile(logFile)) {
                logFile.delete();
            }
        }
    }

    /**
     * 是否是过期的日志文件
     */
    private boolean isPastDueLogFile(File logFile) {
        String fileName = FileUtils.getFileNameNotSuffix(logFile);
        if (fileName.contains("(")) {
            fileName = fileName.substring(0, fileName.indexOf("("));
        }
        // 日志文件创建时间
        long logFileCreateTime = TimeUtils.string2Millis(fileName, LOG_FILE_NAME_DATE_FORMAT);
        // 日志文件名称格式解析错误，删除
        if (logFileCreateTime <= 0) {
            return true;
        }
        // 检测时间是否已经过期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -SAVE_LOG_DAYS);
        return calendar.getTimeInMillis() > logFileCreateTime;
    }

    /**
     * 日志写入器
     */
    private class LogWriter implements Runnable {
        @Override
        public void run() {
            while (isLogWriterRunning.get()) {
                try {
                    // 检查日期发生变化
                    String currentDate = TimeUtils.getNowTimeString();
                    if (TextUtils.isEmpty(mCurrentDate) || !mCurrentDate.equals(currentDate)) {
                        checkDeletePastDueLog();
                        init();
                    }
                    if (mLogFile!=null){
                    FileUtils.writeFileFromString(mLogFile, mLogQueue.take(), true);}
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            isLogWriterRunning.set(false);
        }

        /**
         * 确保运行着
         */
        public void ensureRunning() {
            // 写入线程在运行
            if (isLogWriterRunning.get()) {
                return;
            }
            // 启动线程
            isLogWriterRunning.set(true);
            new Thread(this).start();
        }
    }
}
