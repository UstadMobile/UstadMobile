package com.ustadmobile.core.contentformats.epub.minxhtml

import com.ustadmobile.core.contentformats.epub.minxhtml.MinXhtmlDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * This is a very basic dummy document so we can check incoming XHTML for basic mistakes (e.g.
 * Storyweaver's invalid XHTML). This forces the entire content through the Xml parser, which will
 * catch tag mismatches (eg. missing / on br tags etc).
 */
@XmlSerialName(
    value = "html",
    namespace = NAMESPACE_XHTML
)
@Serializable
class MinXhtmlDocument(
    val body: Body? = null,
    val head: Head? = null,
) {

    companion object {
        const val NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml"
    }
}