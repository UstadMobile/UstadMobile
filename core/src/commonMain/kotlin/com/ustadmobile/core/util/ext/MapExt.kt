package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder

/**
 * Convert the given String - String map into a query String in the form of key1=value1-url-encoded
 */
fun Map<String, String>.toQueryString(): String {
    return this.entries.map { "${UMURLEncoder.encodeUTF8(it.key)}=${UMURLEncoder.encodeUTF8(it.value)}" }.joinToString(separator = "&")
}

/**
 * Where this Map<String, String> are the arguments for a presenter, generate a deep link.
 */
fun Map<String, String>.toDeepLink(endpointUrl: String, viewName: String): String {
    val endpointAndDividerAndView = UMFileUtil.joinPaths(endpointUrl,
        UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER) + viewName
    return endpointAndDividerAndView.appendQueryArgs(toQueryString())
}

/**
 * Puts a value in the receiver Map if it is present in the saved state handle. This can be useful to
 * selectively copy keys from one map to another, whilst avoiding putting the string "null" in
 * by accident
 */
fun MutableMap<String, String>.putFromSavedStateIfPresent(
    savedState: UstadSavedStateHandle,
    key: String
) {
    savedState[key]?.also {
        put(key, it)
    }
}


fun Map<String, List<String>>.firstCaseInsensitiveOrNull(key: String): String? {
    return entries.firstOrNull { it.key.equals(key, true) }?.value?.firstOrNull()
}

fun Map<String, String>.getCaseInsensitiveOrNull(key: String) : String? {
    return entries.firstOrNull { it.key.equals(key, true) }?.value
}


