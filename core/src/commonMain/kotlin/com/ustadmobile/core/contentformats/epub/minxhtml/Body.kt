package com.ustadmobile.core.contentformats.epub.minxhtml

import com.ustadmobile.core.contentformats.epub.minxhtml.MinXhtmlDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "body",
    namespace = NAMESPACE_XHTML
)
class Body
