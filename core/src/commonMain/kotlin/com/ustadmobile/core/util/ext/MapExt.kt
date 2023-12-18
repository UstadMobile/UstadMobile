package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

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

fun <T> MutableMap<String, String>.putEntityAsJson(
    key: String,
    json: Json,
    serializer: SerializationStrategy<T>,
    entity: T?
){
    val entityVal = entity ?: return
    this[key] = json.encodeToString(serializer, entityVal)
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



