package com.feipai.flypai.utils.languageutils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.feipai.flypai.utils.global.ResourceUtils;
import com.feipai.flypai.utils.global.Utils;

import java.util.Locale;

public class LanguageUtil {
    public static void applyLanguage(String newLanguage) {
        Configuration configuration = ResourceUtils.getResource().getConfiguration();
        Locale locale = SupportLanguageUtil.getSupportLanguage(newLanguage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);

        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = ResourceUtils.getResource().getDisplayMetrics();
            ResourceUtils.getResource().updateConfiguration(configuration, dm);
        }
    }

    public static Context attachBaseContext(String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(language);
        } else {
            applyLanguage(language);
            return Utils.context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(String language) {
        Configuration configuration = ResourceUtils.getResource().getConfiguration();
        Locale locale;
        if (TextUtils.isEmpty(language)) {//如果没有指定语言使用系统首选语言
            locale = SupportLanguageUtil.getSystemPreferredLanguage();
        } else {//指定了语言使用指定语言，没有则使用首选语言
            locale = SupportLanguageUtil.getSupportLanguage(language);
        }
        configuration.setLocale(locale);
        return Utils.context.createConfigurationContext(configuration);
    }

    public static boolean isEnglish() {
        Locale locale = ResourceUtils.getResource().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.contains("en");
    }

    /**
     * 是否是简体中文
     */
    public static boolean isZh() {
        Locale locale = ResourceUtils.getResource().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;

    }
}
