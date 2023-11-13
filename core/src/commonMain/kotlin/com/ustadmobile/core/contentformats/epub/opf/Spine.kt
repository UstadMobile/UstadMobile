package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "spine",
    namespace = NS_OPF,
)
class Spine(
    val itemRefs: List<ItemRef> = emptyList(),

    //on EPUB2 the spine element has a toc attribute
    val toc: String? = null,
)
