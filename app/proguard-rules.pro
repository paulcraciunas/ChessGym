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

# Preserve line number information for debugging stack traces (required for Crashlytics)
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name (Crashlytics uses mapping file to restore it)
-renamesourcefileattribute SourceFile

# Prevent R8 from optimizing out public exception classes
-keep public class * extends java.lang.Exception

# Play Core / GMS compile-time annotations not shipped in runtime
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite

# Prevent R8 from renaming or stripping the generated resource pre-warmer file
-keep class com.paulcraciunas.global.resources.PreWarmDrawables {
    public static final com.paulcraciunas.global.resources.PreWarmDrawables INSTANCE;
    public final int[] getList();
}

# Prevent R8 from renaming or stripping out zstd-jni classes and fields
-keep class com.github.luben.zstd.** { *; }

# Prevent R8 from renaming methods and fields used by JNI
-keepclassmembers class com.github.luben.zstd.** {
    native <methods>;
    *** srcPos;
    *** dstPos;
}
