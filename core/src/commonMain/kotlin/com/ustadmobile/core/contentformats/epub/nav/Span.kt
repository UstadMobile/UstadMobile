package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@XmlSerialName(
    value = "span",
    namespace = NAMESPACE_XHTML,
)
@Serializable
class Span(
    @XmlValue
    val content: String = ""
)
