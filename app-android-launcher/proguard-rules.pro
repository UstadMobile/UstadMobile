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

#OrmLite
-keepattributes *DatabaseField*
-keepattributes *DatabaseTable*
-keepattributes *SerializedName*
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }


-dontwarn com.j256.ormlite.android.**
-dontwarn com.j256.ormlite.logger.**
-dontwarn com.j256.ormlite.misc.**

-keep class org.apache.harmony.lang.annotation.** { *; }
-keep interface org.apache.harmony.lang.annotation.** { *; }

#ormlite entities
-keep class com.ustadmobile.nanolrs.ormlite.generated.model.** { *; }
-keep class com.ustadmobile.nanolrs.core.model.** { *; }

-keep class org.kxml2.** { *; }
-keep class org.xmlpull.** { *; }

#ignore .j256.ormlite.android.DatabaseTableConfigUtil: can't find dynamically referenced class org.apache.harmony.lang.annotation.AnnotationFactory, it's in the core lib
#-dontwarn com.j256.ormlite.android.DatabaseTableConfigUtil
#-dontwarn com.j256.ormlite.field.DatabaseFieldConfig

#JodaTime
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-keep class org.slf4j.** { *; }
-keep interface org.slf4j.** { *; }
-dontwarn org.slf4j.**
