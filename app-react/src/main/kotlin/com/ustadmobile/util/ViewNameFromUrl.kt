package com.ustadmobile.util

import kotlinx.browser.window

/**
 * Get current view name from URL
 *
 * NOTE: This was supposed to be an extension function but external functions can't be extended
 */
fun getViewNameFromUrl(url: String? = null): String? {
    val href = url ?: window.location.href
    val hashIndex = href.lastIndexOf("#")
    var viewName = if (hashIndex != -1)
        href.substring(hashIndex + 1).substringAfter("/") else ""

    if (viewName.indexOf("?") != -1)
        viewName = viewName.substringBefore("?")

    return if (viewName.isEmpty() || viewName.startsWith("http")) null else viewName
}