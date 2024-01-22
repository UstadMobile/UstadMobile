package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "content",
    namespace = NAMESPACE_NCX
)
@Serializable
class Content(
    val id: String? = null,
    val src: String,
)
