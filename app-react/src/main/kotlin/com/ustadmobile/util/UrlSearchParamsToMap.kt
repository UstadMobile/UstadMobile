package com.ustadmobile.util

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

/**
 * Converting Url search parameters to a map,
 * this uses JS system built it functions to get args for presenters
 *
 * NOTE: This was supposed to be an extension function but external functions can't be extended
 */
//params is used by js code
@Suppress("UNUSED_VARIABLE")
fun urlSearchParamsToMap(): Map<String,String> {
    val params = URLSearchParams(window.location.href.substringAfter("?"))
    return (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
        .invoke(js("Object.fromEntries(params)"))
        .map { entry -> entry[0] as String to entry[1] as String }.toMap()
}