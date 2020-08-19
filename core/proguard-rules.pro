
#
# These classes are serialized using Gson, hence fields must not be obfuscated
-keep public class com.ustadmobile.core.contentformats.xapi.*{
    <fields>;
}

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
# as per https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
