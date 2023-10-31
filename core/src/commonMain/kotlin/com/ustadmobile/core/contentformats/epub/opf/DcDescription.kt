package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.Package.Companion.NS_DC
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@XmlSerialName(
    value = "description",
    namespace = NS_DC,
)
@Serializable
class DcDescription(
    val lang: String? = null,
    @XmlValue
    val content: String = "",
) {
}