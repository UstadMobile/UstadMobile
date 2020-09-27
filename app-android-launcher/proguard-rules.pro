# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/mike/Android/Sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# umDatabase entities
-keep public class com.ustadmobile.lib.db.entities.**{
       *;
}
-keep public class com.ustadmobile.lib.db.sync.entities.**{
       *;
}
-keep public class com.ustadmobile.core.db.**{
    public <init>(...);
}
-keep public class com.ustadmobile.core.controller.IndexLog{
       *;
}
-keep public class com.ustadmobile.core.controller.IndexLog$IndexEntry{
       *;
}

-keep public class com.toughra.ustadmobile.BuildConfig
-keep public class com.ustadmobile.codec2.Codec2{
        *;
}

#content editor
-keep public class com.ustadmobile.port.android.umeditor.**{
       *;
}

-keep public class com.ustadmobile.core.contentformats.ImportedContentEntryMetaData.*{
    *;
}

 #
 ###################################################################################################
 #### OKHttp3 rules as per
 ## https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro
 ###################################################################################################
 ## JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
 #
 ## A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
 #
 ## Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
 #
 ## OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
 ####

 # Prevent proguard from stripping interface information from TypeAdapterFactory,
 # JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
 -keep class * implements com.google.gson.TypeAdapterFactory
 -keep class * implements com.google.gson.JsonSerializer
 -keep class * implements com.google.gson.JsonDeserializer
