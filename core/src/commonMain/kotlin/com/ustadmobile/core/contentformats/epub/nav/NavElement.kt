package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_OPS
import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * As per the specification, the nav element must contain exactly one ol
 */
@Serializable
@XmlSerialName(
    value = "nav",
    namespace = NAMESPACE_XHTML,
)
class NavElement(
    @XmlSerialName(
        value = "type",
        namespace = NAMESPACE_OPS,
    )
    val epubType: String? = null,
    val orderedList: OrderedList,
)
