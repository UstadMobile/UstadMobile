-keep public class org.xmlpull.v1.*
-keep public interface org.xmlpull.v1.*

#Keep anything with the Serializable annotation. We are using Gson to avoid kotlinx serialization
# errors, but GSON requires classes to be exempted from obfuscation
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# umDatabase entities
-keep public class com.ustadmobile.lib.db.entities.**{
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


-keep public class com.ustadmobile.core.contentformats.metadata.*{
    *;
}

 # Prevent proguard from stripping interface information from TypeAdapterFactory,
 # JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
 -keep class * implements com.google.gson.TypeAdapterFactory
 -keep class * implements com.google.gson.JsonSerializer
 -keep class * implements com.google.gson.JsonDeserializer

# As per Android Gradle Plugin 8 (2/May/2023) - probably OKHTTP related
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder

# As per KodeIn DI docs:
# https://kosi-libs.org/kodein/7.19/framework/android.html#_proguard_configuration
-dontwarn java.lang.invoke.StringConcatFactory

-keep, allowobfuscation, allowoptimization class org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest


# Begin XXHash rules
# XXHash factories uses reflection
-keep class net.jpountz.xxhash.XXHash32JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.XXHash32JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# XXHash : XXHashFactory uses reflection
-keep class net.jpountz.xxhash.XXHash64JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.XXHash64JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash32JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash32JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# XXHash : XXHashFactory uses reflection
-keep class net.jpountz.xxhash.StreamingXXHash64JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash64JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# End XXHash rules

#passkey
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
