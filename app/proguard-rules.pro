# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**
-keep class androidx.media3.** {*;}
-keep interface androidx.media3.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class androidx.multidex.** { *; }
-keep class com.download.video_download.App { *; }
-keep public class * extends android.app.Application
-keep class com.download.video_download.** { *; }
# Keep AndroidX lifecycle components
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }

# Keep Room database classes
-keep class androidx.room.** { *; }
-keep class com.download.video_download.base.room.** { *; }

# Keep data binding classes
-keep class com.download.video_download.databinding.** { *; }

# Keep reflection-used classes
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keepattributes Signature
-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep public class com.android.installreferrer.** { *; }
-keep class com.download.video_download.base.config.cg.** { *; }
-keep class com.download.video_download.base.config.sensor.** { *; }
-keep class com.facebook.** { *; }
-keep interface com.facebook.** { *; }
-keep enum com.facebook.** { *; }
-keepclassmembers class com.facebook.** {
  *;
}

-keepnames class com.facebook.** { *; }
-keepclasseswithmembers class com.facebook.** { *; }
-keep class com.appsflyer.** { *; }
-keep interface com.appsflyer.** { *; }
-keep enum com.appsflyer.** { *; }
-keepclassmembers class com.appsflyer.** {
    static final ** Companion;
    static <fields>;
    static <methods>;
}
-keepattributes KotlinMetadata, InnerClasses, EnclosingMethod, Signature, RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
-keep class com.appsflyer.internal.** { *; }
-keepclassmembers class com.appsflyer.internal.** {
    <init>(...);
    *;
}
-keepclasseswithmembers class com.appsflyer.** {
    native <methods>;
    public <init>(android.content.Context);
}
-keep class com.appsflyer.internal.AFj1cSDK { *; }
-dontwarn com.appsflyer.**
-dontwarn kotlin.**
-dontwarn okhttp3.**
-dontwarn okio.**