package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Represents an EPUB navigation document as per the spec here:
 * https://www.w3.org/submissions/2017/SUBM-epub-packages-20170125/#sec-package-nav
 */
@Serializable
@XmlSerialName(
    value = "html",
    namespace = NAMESPACE_XHTML
)
class NavigationDocument(
    val bodyElement: Body,
) {

    companion object {

        const val NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml"

        const val NAMESPACE_OPS = "http://www.idpf.org/2007/ops"
    }
}