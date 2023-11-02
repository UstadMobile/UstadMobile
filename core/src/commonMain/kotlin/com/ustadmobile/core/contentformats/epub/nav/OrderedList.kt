package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "ol",
    namespace = NAMESPACE_XHTML
)
class OrderedList(
    val listItems: List<ListItem>
)
