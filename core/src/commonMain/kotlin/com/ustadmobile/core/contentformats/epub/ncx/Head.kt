package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import com.ustadmobile.core.contentformats.epub.opf.Metadata
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "head",
    namespace = NAMESPACE_NCX,
)
@Serializable
class Head(
    val metadata: List<Metadata> = emptyList(),
)
