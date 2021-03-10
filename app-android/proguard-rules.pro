-keep public class org.xmlpull.v1.*
-keep public interface org.xmlpull.v1.*

#Keep anything with the Serializable annotation. We are using Gson to avoid kotlinx serialization
# errors, but GSON requires classes to be exempted from obfuscation
-keep @kotlinx.serialization.Serializable class * {
    *;
}
