package com.ustadmobile.core.catalog.contenttype

data class XhtmlFixResult(
    val wasValid: Boolean,
    val xhtml: String,
)

/**
 * Utility that is implemented on the underlying platform to cleanup bad XHTML (we're looking at
 * you, Storyweaver).
 *
 * Will check if the given xhtml is valid. If not, pass through a library to clean it up (e.g. Jsoup)
 */
interface XhtmlFixer {

    fun fixXhtml(xhtml: String): XhtmlFixResult

}
