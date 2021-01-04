package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.UMURLEncoder
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Convert the given String - String map into a query String in the form of key1=value1-url-encoded
 */
fun Map<String, String>.toQueryString(): String {
    return this.entries.map { "${it.key}=${UMURLEncoder.encodeUTF8(it.value)}" }.joinToString(separator = "&")
}

fun <T> MutableMap<String, String>.putEntityAsJson(key: String, serializer: SerializationStrategy<T>?, entity: T?){
    val entityVal = entity ?: return
    val jsonStr = (defaultSerializer().write(entityVal) as? TextContent)?.text ?: return
    this[key] = jsonStr
}


fun Map<String, String>.convertToJsonObject(): JsonObject{
   return JsonObject(this.map { entry -> Pair(entry.key, JsonPrimitive(entry.value)) }.toMap())
}

/**
 * Puts a value in the receiver Map if it is present in the other map. This can be useful to
 * selectively copy keys from one map to another, whilst avoiding putting the string "null" in
 * by accident
 */
fun <K, V> MutableMap<K, V>.putFromOtherMapIfPresent(otherMap: Map<K, V>, keyVal: K) {
    val otherMapVal = otherMap[keyVal]
    if(otherMapVal != null) {
        put(keyVal, otherMapVal)
    }
}

/**
 * No overwrite put
 */
fun <K, V> MutableMap<K, V>.putIfNotAlreadySet(key: K, keyVal: V) {
    if(!containsKey(key))
        put(key, keyVal)
}
