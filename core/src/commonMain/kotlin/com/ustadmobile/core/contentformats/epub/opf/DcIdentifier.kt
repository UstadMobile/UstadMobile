package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_DC
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@XmlSerialName(
    value = "identifier",
    namespace = NS_DC,
)
@Serializable
class DcIdentifier(
    val id: String? = null,
    @XmlValue
    val content: String = "",
)

