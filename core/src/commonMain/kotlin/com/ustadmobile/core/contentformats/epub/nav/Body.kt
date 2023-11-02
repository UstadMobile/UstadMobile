package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "body",
    namespace = NAMESPACE_XHTML
)
@Serializable
class Body(
    val navigationElements: List<NavElement>,
)
