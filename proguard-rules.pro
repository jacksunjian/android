# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\SDK\android\android-sdk-windows/tools/proguard/proguard-android.txt
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

-keepattributes Signature,*Annotation*,EnclosingMethod,SourceFile,LineNumberTable

-dontnote android.support.**
-dontnote com.alibaba.**
-dontnote com.android.**
-dontnote com.fasterxml.**
-dontnote com.google.**
-dontnote javassist.**
-dontwarn javassist.**
-dontnote org.nustaq.**
-dontwarn org.nustaq.**
-dontnote org.objenesis.**
-dontwarn org.objenesis.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.github.junrar.**
-dontwarn com.squareup.**
-dontwarn com.raizlabs.**

-dontwarn com.onyx.android.sdk.ui.dialog.DialogLoading
-dontwarn com.onyx.android.sdk.ui.dialog.DialogReaderMenu
-dontwarn com.onyx.android.sdk.scribble.request.shape.PenStateChangeRequest

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class * implements android.os.Parcelable { *; }

-keep class * implements java.io.Serializable { *; }

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

#for eventBus
-keepnames class org.greenrobot.eventbus.** { *; }
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }
-keep class * extends com.raizlabs.android.dbflow.converter.TypeConverter { *; }
-keep class * extends com.raizlabs.android.dbflow.structure.BaseModel { *; }

-keep class com.alibaba.fastjson.** { *; }

# Retain generated class which implement Unbinder.
-keep public class * implements butterknife.Unbinder { public <init>(...); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinding.
-keep class butterknife.*
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-dontwarn butterknife.internal.ButterKnifeProcessor
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# for wechat
-keep class com.tencent.mm.opensdk.** { *; }
-keep class com.tencent.wxop.** { *; }
-keep class com.tencent.mm.sdk.** { *; }


