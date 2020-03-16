package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.UMURLEncoder

/**
 * Convert the given String - String map into a query String in the form of key1=value1-url-encoded
 */
fun Map<String, String>.toQueryString(): String {
    return this.entries.map { "${it.key}=${UMURLEncoder.encodeUTF8(it.value)}" }.joinToString(separator = "&")
}