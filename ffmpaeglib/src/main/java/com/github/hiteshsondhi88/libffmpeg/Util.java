package com.github.hiteshsondhi88.libffmpeg;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Util {

    static boolean isDebug(Context context) {
        return (0 != (context.getApplicationContext().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
    }

    static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    static String convertInputStreamToString(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e("error converting input stream to string", e);
        }
        return null;
    }

    static void destroyProcess(Process process) {
        if (process != null)
            process.destroy();
    }

    static boolean killAsync(AsyncTask asyncTask) {
        return asyncTask != null && !asyncTask.isCancelled() && asyncTask.cancel(true);
    }

    static boolean isProcessCompleted(Process process) {
        try {
            if (process == null) return true;
            process.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            // do nothing
        }
        return false;
    }

    /**
     * 截取文本中间
     */
    public static String getStrSub(String str, String start, String end) {
        int startp = str.indexOf(start) + start.length();
        int endp = str.indexOf(end, startp);
        String tmp = "";
        try {
            tmp = str.substring(startp, endp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp;
    }

    /**
     * 00：00：00 转 1321312 格式
     */
    public static long strFormTime(String str) {
        String[] arr = str.split(":");
        if (arr.length < 3)
            return 0;
        long t = strParseInt(arr[0]) * 3600 + strParseInt(arr[1]) * 60 + Math.round(strParseDouble(arr[2]));
        return t;
    }

    /**
     * str转int
     */
    public static int strParseInt(String str) {
        int tmp = 0;
        try {
            tmp = Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * str转double
     */
    public static double strParseDouble(String str) {
        double tmp = 0;
        try {
            tmp = Double.parseDouble(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

}
