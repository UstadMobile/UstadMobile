package com.ustadmobile.util

import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.util.ext.requirePostfix
import web.url.URLSearchParams

const val SEARCH_PARAM_KEY_API_URL = "apiUrl"

/**
 * Determines the API endpoint URL based on the browser location href and searchParams. If the
 * apiUrl is explicitly specified in the searchParams, this will override. Otherwise look for the
 * /#/ and /umapp to figure out the API endpoint URL.
 *
 * @param href browser location e.g. location.href
 * @param searchParams URLSearchParams
 */
fun resolveEndpoint(
    href: String,
    searchParams: URLSearchParams,
): String {
    val searchParamApiUrl = searchParams[SEARCH_PARAM_KEY_API_URL]
    return if(searchParamApiUrl != null) {
        searchParamApiUrl
    }else if(href.contains(UstadUrlComponents.DEFAULT_DIVIDER)) {
        href.substringBefore(UstadUrlComponents.DEFAULT_DIVIDER)
            .removeSuffix("/umapp").requirePostfix("/")
    }else if(href.contains("/umapp/"))
        href.substringBefore("/umapp/").requirePostfix("/")
    else {
        href
    }
}
