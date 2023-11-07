package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@XmlSerialName(
    value = "text",
    namespace = NAMESPACE_NCX,
)
@Serializable
class Text(
    @XmlValue
    val content: String = ""
)
