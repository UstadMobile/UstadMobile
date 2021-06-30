package com.ustadmobile.util

import kotlinx.browser.window

/**
 * Get current view name from URL
 *
 * NOTE: This was supposed to be an extension function but external functions can't be extended
 */
fun getViewNameFromUrl(url: String? = null): String? {
    val href = url ?: window.location.href
    val viewName = href.substringAfterLast("#/", "")
        .substringBeforeLast("?")
    return if (viewName.isEmpty() || viewName.startsWith("http")) null else viewName
}