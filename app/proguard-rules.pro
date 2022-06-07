# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\xiongli\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
 #优化  不优化输入的类文件
-dontoptimize
 #预校验
-dontpreverify
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*


# 保持哪些类不被混淆
-keep class android.** {*; }
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
#-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.** { *; }
#如果引用了v4或者v7包

-dontwarn android.support.*

##忽略警告
#-ignorewarning

-dontwarn butterknife.compiler.**
-dontwarn com.google.auto.common.**
-dontwarn com.google.common.**
-dontwarn com.google.auto.service.processor.AutoServiceProcessor
-dontwarn com.squareup.javapoet.**
-dontwarn AutoContentFrameLayout
-dontwarn butterknife.compiler.BindingClass
-dontwarn com.squareup.javapoet.ArrayTypeName
-dontwarn com.squareup.javapoet.ClassName
-dontwarn com.squareup.javapoet.CodeWriter
-dontwarn com.squareup.javapoet.FieldSpec
-dontwarn com.squareup.javapoet.JavaFile
-dontwarn com.squareup.javapoet.MethodSpec
-dontwarn com.squareup.javapoet.NameAllocator
-dontwarn com.squareup.javapoet.ParameterSpec
-dontwarn com.squareup.javapoet.TypeName
-dontwarn com.squareup.javapoet.TypeSpec
-dontwarn com.squareup.javapoet.TypeVariableName
-dontwarn com.squareup.javapoet.WildcardTypeName
-dontwarn jp.wasabeef.recyclerview.animators.BaseItemAnimator
-dontwarn uk.co.senab.photoview.PhotoViewAttacher
-dontwarn uk.co.senab.photoview.gestures.CupcakeGestureDetector

#####################记录生成的日志数据,gradle build时在本项目根目录输出################
 #混淆时是否记录日志
-verbose
#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt


#####################记录生成的日志数据，gradle build时 在本项目根目录输出-end################


#####混淆保护自己项目的部分代码以及引用的第三方jar包library - start #######


#如果不想混淆 keep 掉  保留一个完整的包
#-keep class com.lippi.recorder.iirfilterdesigner.** {*; }
#项目特殊处理代码
#忽略警告
#-dontwarn com.lippi.recorder.utils**


#dialog
-keep class me.drakeet.materialdialog.** { *; }

#下拉刷新
-keep class in.srain.cube.views.ptr.** { *; }

#umeng
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keep class com.umeng.**
-keep public class com.feipai.flypai.R$*{
    public static final int *;
}
-keep class * extends com.umeng.socialize.net.base.SocializeReseponse {
   *;
}
-keep public class com.umeng.fb.ui.ThreadView {
}
-dontwarn com.umeng.**
-dontwarn org.apache.commons.**
-keep public class * extends com.umeng.**
-keep class com.umeng.** {*; }


# 微信
-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}
-keep class com.tencent.mm.opensdk.** {*;}
-keep class com.tencent.wxop.** {*;}
-keep class com.tencent.mm.sdk.** {*;}
#友盟统计
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#忽略recyclerview
-dontwarn android.support.v7.recyclerview.**




#实体类不混淆
-keep class com.feipai.flypai.beans.** { *; } #不能混淆 否则注解无效
-keep class com.feipai.flypai.base.** { *; } #不能混淆 否则注解无效
#bean不被混淆
-keepattributes EnclosingMethod

####混淆保护自己项目的部分代码以及引用的第三方jar包library-end####


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}



#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
    public static <methods>;
}


#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}



-keepclassmembers class * extends android.app.Service { #保持类成员
   public void onCreate();
}


#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}




-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}


#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}
 -keep class **.R$* { *; }
#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

#ijkplayer代码混淆
#-keep com.dou361.ijkplayer.** {*;}

#-libraryjars  ../videoplayer/src/main/jniLibs/arm64-v8a/libffmpeg.so
#-libraryjars  ../imageeditlibrary/src/main/jniLibs/arm64-v8a/libphotoprocessing.so


#-libraryjars ../videoplayer/src/main/jniLibs/armeabi-v7a/libffmpeg.so
#-libraryjars ../imageeditlibrary/src/main/jniLibs/armeabi-v7a/libphotoprocessing.so


##包含的panoLib.so库需要被忽略混淆
-keep class opencv.** {*;}

-keep class com.dou361.**{*;}
-keep class tv.danmaku.ijk.media.**{*;}

-keep  class wseemann.media.**{*;}

-keep class com.videoplayer.**{*;}



-keep class com.qiniu.**{*;}
-keep class com.qiniu.**{public <init>();}


#3D 地图 V5.0.0之后：
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.**{*;}
-keep   class com.amap.api.trace.**{*;}

#定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

#搜索
#-keep   class com.amap.api.services.**{*;}

#2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

#导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}

#bugly混淆
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#mob
-keep class com.mob.**{*;}
-keep class cn.smssdk.**{*;}
-dontwarn com.mob.**

#保持源码的行号、源文件信息不被混淆 方便在崩溃日志中查看
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

#uCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }


# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# okhttp
-dontwarn okio.**

-keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
}

-keep class com.google.**{*;}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

