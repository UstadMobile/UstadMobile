package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Represents an NCX table of contents as per the spec here:
 * https://www.daisy.org/z3986/2005/Z3986-2005.html#NCX
 */
@Serializable
@XmlSerialName(
    value = "ncx",
    namespace = NAMESPACE_NCX,
)
class NcxDocument(
    val head: Head,
    val docTitle: DocTitle,
    val navMap: NavMap,
) {

    companion object {

        const val NAMESPACE_NCX = "http://www.daisy.org/z3986/2005/ncx/"

    }
}
