package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "li",
    namespace = NAMESPACE_XHTML,
)
class ListItem(
    val anchor: Anchor? = null,
    val span: Span? = null,
    val orderedList: OrderedList? = null,
)

