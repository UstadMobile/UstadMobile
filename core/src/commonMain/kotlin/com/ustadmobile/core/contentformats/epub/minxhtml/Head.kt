package com.ustadmobile.core.contentformats.epub.minxhtml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "body",
    namespace = MinXhtmlDocument.NAMESPACE_XHTML
)
class Head
