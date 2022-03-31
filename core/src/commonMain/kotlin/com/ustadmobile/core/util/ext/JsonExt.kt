package com.ustadmobile.core.util.ext

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

//Shorthand for encoding a map of strings
fun Json.encodeStringMapToString(stringMap: Map<String, String>): String {
    return encodeToString(MapSerializer(String.serializer(), String.serializer()), stringMap)
}

fun Json.decodeStringMapFromString(string: String): Map<String, String> {
    return decodeFromString(MapSerializer(String.serializer(), String.serializer()), string)
}

