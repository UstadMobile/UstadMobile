package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.UMURLEncoder
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.serialization.SerializationStrategy

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