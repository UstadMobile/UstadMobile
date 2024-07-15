package com.ustadmobile.core.util.ext

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

//Shorthand for encoding a map of strings
fun Json.encodeStringMapToString(stringMap: Map<String, String>): String {
    return encodeToString(MapSerializer(String.serializer(), String.serializer()), stringMap)
}

fun Json.decodeStringMapFromString(string: String): Map<String, String> {
    return decodeFromString(MapSerializer(String.serializer(), String.serializer()), string)
}

/**
 * Handle a situation where a JsonElement to decode could be a single object or a list of objects
 * of the same type (eg Xapi context activities etc).
 */
fun <T> Json.decodeListOrSingleObjectAsList(
    serializer: KSerializer<T>,
    element: JsonElement,
): List<T>? {
    return when {
        element is JsonArray -> decodeFromJsonElement(
            ListSerializer(serializer), element
        )
        element is JsonObject -> listOf(
            decodeFromJsonElement(
                serializer, element
            )
        )
        else -> null
    }
}
