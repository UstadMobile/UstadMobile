-keep public interface com.ustadmobile.nanolrs.core.model.*
-keep public class com.ustadmobile.nanolrs.ormlite.generated.model.*
-keep public class org.xmlpull.v1.*
-keep public interface org.xmlpull.v1.*

-keepattributes *DatabaseField*
-keepattributes *DatabaseTable*
-keepattributes *SerializedName*
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-dontwarn com.j256.**

#content editor
-keep public class com.ustadmobile.port.android.umeditor.**{
       *;
}