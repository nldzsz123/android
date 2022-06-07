package com.feipai.flypai.utils.global;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource相关工具类
 */
public class ResourceUtils {

    private ResourceUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 获取资源实体
     */
    public static Resources getResource() {
        return Utils.context.getResources();
    }

    /**
     * 获取到字符数组
     */
    public static String[] getStringArray(int res) {
        return getResource().getStringArray(res);
    }

    public static int getIndexInArray(int res, String str) {
        String[] strings = getStringArray(res);
        for (int i = 0; i < strings.length; i++) {
            if (str.equals(strings[i])) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 获取到字符数组
     */
    public static List<String> getStringList(int res) {
        return new ArrayList<>(Arrays.asList(ResourceUtils.getStringArray(res)));
    }

    /**
     * 通过资源ID获取Drawable
     */
    public static Drawable getDrawabe(int id) {
        return getResource().getDrawable(id);
    }

    /**
     * 将本地资源图片大小缩放
     *
     * @param resId
     * @param w
     * @param h
     * @return
     */
    public static Bitmap getDrawabeToBitmap(int resId, int w, int h) {
        Bitmap oldBmp = BitmapFactory.decodeResource(getResource(), resId);
        Bitmap newBmp = Bitmap.createScaledBitmap(oldBmp, w, h, true);
//        Drawable drawable = new BitmapDrawable(getResource(), newBmp);
        return newBmp;
    }

    /**
     * 通过字符串ID获取 String
     */
    public static String getString(int id) {
        return getResource().getString(id);
    }

    /**
     * 获取格式化的字符串
     */
    public static String getString(int resId, Object... formatArgs) {
        return ResourceUtils.getResource().getString(resId, formatArgs);
    }

    /**
     * 通过Dimension ID 获取大小
     */
    public static float getDimension(int id) {
        return getResource().getDimension(id);
    }

    /**
     * 通过Dimension ID 获取像素大小
     */
    public static int getDimensionPixelSize(int id) {
        return getResource().getDimensionPixelSize(id);
    }

    /**
     * 获取颜色id
     */
    public static int getColor(int colorId) {
        return getResource().getColor(colorId);
    }

    /**
     * 获取XML中ID数组
     */
    public static int[] getResIds(int id) {
        TypedArray ar = getResource().obtainTypedArray(id);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();

        return resIds;
    }

    /**
     * inflate一个资源
     */
    public static <T extends View> T inflate(int id) {
        return (T) View.inflate(Utils.context, id, null);
    }

    /**
     * inflate一个资源进父容器
     *
     * @param id   资源ID
     * @param root 父容器
     */
    public static <T extends View> T inflate(int id, ViewGroup root) {
        return (T) View.inflate(Utils.context, id, root);
    }

    public static <T extends View> T findViewById(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * get an asset using ACCESS_STREAMING mode. This provides access to files that have been bundled with an
     * application as assets -- that is, files placed in to the "assets" directory.
     *
     * @param context
     * @param fileName The name of the asset to open. This name can be hierarchical.
     * @return
     */
    public static String geFileFromAssets(Context context, String fileName) {
        if (context == null || StringUtils.isEmpty(fileName)) {
            return null;
        }

        StringBuilder s = new StringBuilder("");
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                s.append(line);
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /***
     * @param assetFileName 资源文件名
     *
     */
    public static byte[] readAssetsFileToByte(Context mc, String assetFileName) {
        InputStream is = null;
        try {
            is = mc.getAssets().open(assetFileName);
            int len = is.available();
            if (len > 0) {
                byte[] buffer = new byte[len];
                is.read(buffer);
                return buffer;
            }
        } catch (IOException e) {
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * get content from a raw resource. This can only be used with resources whose value is the name of an asset files
     * -- that is, it can be used to open drawable, sound, and raw resources; it will fail on string and color
     * resources.
     *
     * @param context
     * @param resId   The resource identifier to open, as generated by the appt tool.
     * @return
     */
    public static String geFileFromRaw(Context context, int resId) {
        if (context == null) {
            return null;
        }

        StringBuilder s = new StringBuilder();
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(resId));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                s.append(line);
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * same to {@link ResourceUtils#geFileFromAssets(Context, String)}, but return type is List<String>
     *
     * @param context
     * @param fileName
     * @return
     */
    public static List<String> geFileToListFromAssets(Context context, String fileName) {
        if (context == null || StringUtils.isEmpty(fileName)) {
            return null;
        }

        List<String> fileContent = new ArrayList<String>();
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.add(line);
            }
            br.close();
            return fileContent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * same to {@link ResourceUtils#geFileFromRaw(Context, int)}, but return type is List<String>
     *
     * @param context
     * @param resId
     * @return
     */
    public static List<String> geFileToListFromRaw(Context context, int resId) {
        if (context == null) {
            return null;
        }

        List<String> fileContent = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(resId));
            reader = new BufferedReader(in);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * same to {@link ResourceUtils#getUrlListFramAssetsDir(Context, String)}, but return type is Map<String, String>
     *
     * @param context
     * @param dir     assets根目录
     * @return
     */
    public static Map<String, String> getUrlListFramAssetsDir(Context context, String dir) {
        if (context == null || StringUtils.isEmpty(dir)) {
            return null;
        }
        Map<String, String> urls = new HashMap<>();
        try {
            String[] fileNames = context.getAssets().list(dir);
            if (CollectionUtils.isNotNullOrEmptyArray(fileNames)) {
                for (String name : fileNames) {
                    urls.put(name, dir + File.separator + name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }

}

