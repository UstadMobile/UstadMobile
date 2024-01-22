package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "navPoint",
    namespace = NAMESPACE_NCX,
)
class NavPoint(
    val navLabels: List<NavLabel>,
    val content: Content,
    val id: String,
    val childPoints: List<NavPoint>,
)

