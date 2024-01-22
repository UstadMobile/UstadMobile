package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "item",
    namespace = NS_OPF,
)
@Serializable
class Item(
    val id: String,
    val href: String,
    val properties: String? = null,
    @XmlSerialName(value = "media-type")
    val mediaType: String,
)
