package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName(
    value = "a",
    namespace = NAMESPACE_XHTML,
)
class Anchor(

    @XmlValue
    val content: String = "",

    val href: String = "",

)
