package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "meta",
    namespace = NS_OPF,
)
@Serializable
class Meta(
    val name: String? = null,
    val content: String? = null,
)