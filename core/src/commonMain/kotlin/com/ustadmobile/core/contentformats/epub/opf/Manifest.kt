package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.Package.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "manifest",
    namespace = NS_OPF,
)
@Serializable
class Manifest(
    val items: List<Item> = emptyList()
) {
}