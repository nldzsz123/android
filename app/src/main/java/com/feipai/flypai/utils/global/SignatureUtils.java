package com.feipai.flypai.utils.global;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;

/**
 * 获取签名MD5
 */
public class SignatureUtils {

    private SignatureUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 获取APK包的签名
     */
    public static String getSign(Context context, String packageName) {
        Signature[] signs = getRawSignature(context, packageName);
        if (signs == null || signs.length == 0) {
            return "";
        } else {
            Signature sign = signs[0];
            return getMessageDigest(sign.toByteArray());
        }
    }

    public static Signature[] getRawSignature(Context context, String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        PackageManager pkgMgr = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pkgMgr.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        if (info == null) {
            return null;
        }
        return info.signatures;
    }

    public static String getMessageDigest(byte[] bytes) {
        char[] chars = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            byte[] disgets = messageDigest.digest();
            int i = disgets.length;
            char[] arrayOfChar2 = new char[i * 2];
            int j = 0;
            int k = 0;
            while (true) {
                if (j >= i)
                    return new String(arrayOfChar2);
                int m = disgets[j];
                int n = k + 1;
                arrayOfChar2[k] = chars[(0xF & m >>> 4)];
                k = n + 1;
                arrayOfChar2[n] = chars[(m & 0xF)];
                j++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getRawDigest(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            byte[] digests = messageDigest.digest();
            return digests;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
