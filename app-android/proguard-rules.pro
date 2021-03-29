-keep public class org.xmlpull.v1.*
-keep public interface org.xmlpull.v1.*

#Keep anything with the Serializable annotation. We are using Gson to avoid kotlinx serialization
# errors, but GSON requires classes to be exempted from obfuscation
-keep @kotlinx.serialization.Serializable class * {
    *;
}


# Begin rules as per Kotlinx Serialization README
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

 #Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class com.ustadmobile.**$$serializer { *; } # <-- change package name to your app's

-keepclassmembers class com.ustadmobile.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.ustadmobile.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}
# End rules as per Kotlinx Serialization README