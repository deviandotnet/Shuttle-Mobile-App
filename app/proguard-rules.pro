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


# Keep Gson annotations
-keepattributes *Annotation*

# Keep all models with Gson serialization (most DTOs have no @SerializedName, so field names must survive)
-keep class com.example.shuttleapp.data.network.response.** { *; }
-keep class com.example.shuttleapp.data.network.request.** { *; }

# security-crypto (Tink) references compile-only errorprone annotations
-dontwarn com.google.errorprone.annotations.**

# SQLCipher: native code looks these classes up by name
-keep class net.zetetic.database.** { *; }

# Keep fields annotated with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
