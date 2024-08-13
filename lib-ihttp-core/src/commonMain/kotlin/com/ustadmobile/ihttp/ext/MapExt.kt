package com.ustadmobile.ihttp.ext

fun Map<String, String>.getCaseInsensitiveOrNull(key: String) : String? {
    return entries.firstOrNull { it.key.equals(key, true) }?.value
}