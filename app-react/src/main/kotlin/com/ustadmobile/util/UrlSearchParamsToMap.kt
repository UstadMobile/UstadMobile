package com.ustadmobile.util

import kotlinx.browser.window

/**
 * Converting Url search parameters to a map,
 * this uses JS system built it functions to get args for presenters
 *
 * NOTE: This was supposed to be an extension function but external functions can't be extended
 */
//queryParts is used by js code
@Suppress("UNUSED_VARIABLE")
fun urlSearchParamsToMap(href: String? = null): Map<String,String> {
    val queryParts = (href ?: window.location.href).substringAfter("?", "")
    return when(queryParts.isEmpty()){
        true -> mapOf()
        else -> (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
            .invoke(js("Object.fromEntries(new URLSearchParams(queryParts))"))
            .map {
                entry -> entry[0] as String to entry[1] as String
            }.toMap()
    }
}