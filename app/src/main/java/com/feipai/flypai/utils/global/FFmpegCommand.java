package com.feipai.flypai.utils.global;

import android.content.Context;

import com.feipai.flypai.utils.MLog;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.Util;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.Arrays;

public class FFmpegCommand {

    static FFmpegCommand instance;

    private static final String CMD_CUT = "-ss&%s&-y&-i&%s&-t&%s&-vcodec&copy&-acodec&copy&%s";
    private static final String CMD_WIPE = "-i&%s&-ss&%s&-t&%s&-i&%s&-an&-vcodec&copy&-y&%s";//去除原声
    //    -i ...1.mp4 -i ....2.mp3 -c:v copy -c:a copy -map  0:v -map 1:a combine.mp4
//    {"-i",videoFileAbsolutePath,"-i",audioFileAbsolutePath, "-c:v", "copy", "-c:a", "aac","-shortest", destinationAbsolutePath};

    private static final String CMD_MERGE =
//            "-i&%s&-i&%s&-c:a&aac&-c:v&copy&-y&%s";
            "-ss&%s&-i&%s&-i&%s&-t&%s&-c:v&copy&-c:a&aac&-map&0:v&-map&1:a&-y&%s";//去除原声,并合成
    //    private static final String CMD_MERGE = "-y&-i&%s&-ss&%s&-t&%s&-i&%s&-filter_complex&[0:a] pan=stereo|c0=1*c0|c1=1*c1 [a1], [1:a] pan=stereo|c0=1*c0|c1=1*c1 [a2],[a1][a2]amix=duration=first,pan=stereo|c0<c0+c1|c1<c2+c3,pan=mono|c0=c0+c1[a]&-map&[a]&-map&0:v&-c:v&copy&-c:a&aac&-strict&-2&-ac&1&%s";
    private static final String CMD_COMPRESS = "-y&-i&%s&-strict&experimental&-s&160x128&-r&25&-aspect&16:9&-ab&48000&-ac&2&-ar&22050&-b&500k&%s";
    private static final String CMD_AVI_TO_MP4 = "-y&-i&%s&-s&960x540&-vcodec&h264&-ab&32K&-ar&24000&-acodec&aac&-y&%s";
    //"-y&-i&%s&-c:v&libx264&-crf&19&-preset&slow&-c:a&aac&-b:a&192k&-ac&2&-y&%s";
//            "-y&-i&%s&-c&copy&-map&0";
    private static final int BUFSIZE = 1024 * 5;
    private static FFmpeg mFmpeg;
    private long mProgressMax;
    private CommandCallback mListener;
    private boolean isLoadLibSuccess;

    public FFmpegCommand() {
        try {
            init();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static FFmpegCommand getInstance() {
        if (instance == null) {
            synchronized (FFmpegCommand.class) {
                if (instance == null) {
                    instance = new FFmpegCommand();
                }
            }
        }
        return instance;
    }


    private void init() throws FFmpegNotSupportedException {
        if (mFmpeg == null) {
            synchronized (FFmpegCommand.class) {
                if (mFmpeg == null) {
                    mFmpeg = FFmpeg.getInstance(Utils.context);
                    mFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                        @Override
                        public void onFailure() {
                            super.onFailure();
                            isLoadLibSuccess = false;
                            if (mListener != null) mListener.onFailure();
                            MLog.log("ffmpeg库加载失败");
                        }

                        @Override
                        public void onSuccess() {
                            super.onSuccess();
                            isLoadLibSuccess = true;
                            if (mListener != null) mListener.onLoadLibSuccess(true);
                            MLog.log("ffmpeg库加载成功");
                        }
                    });
                }
            }

        }

    }

    public void setCallback(CommandCallback callback) {
        this.mListener = callback;
    }


    /**
     * 剪切
     **/
    public void cut(String inPath, String outPath, String startTime, String duration) {
        String cmd = String.format(CMD_CUT, startTime, fromPath(inPath), 10, fromPath(outPath));
        MLog.log("视频剪切：" + cmd);
        execute(cmd);

    }

    public void wipeVoice(String inPath, String outPath) {
        String cmd = String.format(CMD_WIPE, fromPath(inPath), fromPath(outPath));
        MLog.log("去除原声:" + cmd);
        execute(cmd);

    }

    public void aviToMp4(String input, String output) {
        String cmd = String.format(CMD_AVI_TO_MP4, fromPath(input), fromPath(output));
        MLog.log("AVI转换成MP4：" + cmd);
        execute(cmd);
    }

    /**
     * 合并
     */
    public void merge(String inPath, String outPath, String mp3Path, String duration) {
//        "-ss&%s&-i&%s&-t&%s&-i&%s&-c:v&copy&-c:a&aac&-map&0:v&-map&1:a&-y&%s";
        String cmd = String.format(CMD_MERGE, "00:00:00", fromPath(inPath), fromPath(mp3Path), duration, fromPath(outPath));
        MLog.log("添加背景音乐：" + cmd);
        execute(cmd);
    }


    private void execute(String cmd) {
        String[] cmds = cmd.split("&");
        MLog.log("转码cmd" + Arrays.asList(cmds));
        try {
            mFmpeg.execute(cmds, mResponseHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private FFmpegExecuteResponseHandler mResponseHandler = new FFmpegExecuteResponseHandler() {
        @Override
        public void onSuccess(String message) {
            MLog.log("onSuccess:" + message);
//            if (mListener != null) {
//                mListener.onSuccess();
//            }
        }

        @Override
        public void onProgress(String message) {
            int i = progressHandler(message);
            if (mListener != null && i != 0)
                mListener.onProgress(i);
        }

        @Override
        public void onFailure(String message) {
            MLog.log("onFailure:" + message);
            if (mListener != null) {
                mListener.onFailure();
            }
        }

        @Override
        public void onStart() {
            MLog.log("onStart");
        }

        @Override
        public void onFinish() {
            MLog.log("onFinish");
            if (mListener != null) {
                mListener.onProgress(100);
                mListener.onFinish();
            }
        }
    };

    public int progressHandler(String str) {
        int index = str.indexOf("frame=");
        int haf = 0;
        String size = "";
        if (index != -1) {
            String tmp = Util.getStrSub(str, "size=", "bitrate=");

            if (tmp == null || tmp.length() == 0)
                return 100;
            String[] arr = tmp.split("time=");
            if (arr.length < 2)
                return 100;
            String time = arr[1].trim();
            long t = Util.strFormTime(time);
            if (mProgressMax != 0 && t != 0) {
                haf = Math.round(t * 100 / mProgressMax);
            }
        }
        index = str.indexOf("Duration:");
        if (index != -1) {
            String duration = Util.getStrSub(str, "Duration:", ", start").trim();
            mProgressMax = Util.strFormTime(duration);
        }
        return haf;
    }

    private String fromPath(String path) {
        return path;
    }


    public void exit() {
        mFmpeg.killRunningProcesses();
    }

    public boolean isLoadLibSuccess() {
        return isLoadLibSuccess;
    }

    public interface CommandCallback {
        void onLoadLibSuccess(boolean isSuccess);

        void onProgress(int progress);

        void onFinish();

        void onFailure();

    }
}
