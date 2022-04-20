package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.serialization.SerializationStrategy

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

fun <T> MutableMap<String, String>.putEntityAsJson(key: String, serializer: SerializationStrategy<T>?, entity: T?){
    val entityVal = entity ?: return
    val jsonStr = (defaultSerializer().write(entityVal) as? TextContent)?.text ?: return
    this[key] = jsonStr
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



/**
 * Save arguments to the bundle to tell the destination view where it should save results. If
 * these arguments are already in the backState arguments, then they will simply be copied over. If
 * not the arguments are set so that the data is saved to the SavedStateHandle of the given NavBackStateEntry
 *
 * e.g. When the user goes directly from fragment a to b, b saves the result to the backstackentry
 * of a.
 * When the user goes from fragment a to b, and then b to c, c saves the result to the backstackentry
 * of a directly (e.g. when the user is presented with a list, and then chooses to create a new entity).
 */
fun MutableMap<String, String>.putResultDestInfo(
    backState: UstadBackStackEntry,
    destinationResultKey: String,
    overwriteDest: Boolean = false
) {
    val backStateArgs = backState.arguments
    val effectiveDestViewName = backStateArgs.takeIf{ !overwriteDest }
        ?.get(UstadView.ARG_RESULT_DEST_VIEWNAME) ?: backState.viewName
    put(UstadView.ARG_RESULT_DEST_VIEWNAME, effectiveDestViewName)

    val effectiveDestinationKey = backStateArgs.takeIf{ !overwriteDest }
        ?.get(UstadView.ARG_RESULT_DEST_KEY) ?: destinationResultKey
    put(UstadView.ARG_RESULT_DEST_KEY, effectiveDestinationKey)
}

/**
 * Determine if this list is operating in picker mode or browse mode according to arguments.
 * This can be set explicitly using UstadView.ARG_LISTMODE. If not explicitly set, it will
 * default to PICKER mode when  there is a result destination key (e.g. a navigateForResult is
 * in progress), BROWSER otherwise.
 */
fun Map<String, String>.determineListMode(): ListViewMode {
    val listModeArg = get(UstadView.ARG_LISTMODE)
    if(listModeArg != null)
        return listModeArg.let { ListViewMode.valueOf(it) }

    if(containsKey(UstadView.ARG_RESULT_DEST_KEY))
        return ListViewMode.PICKER
    else
        return ListViewMode.BROWSER
}
