package com.ustadmobile.core.contentformats.epub.ncx

import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument.Companion.NAMESPACE_NCX
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "docTitle",
    namespace = NAMESPACE_NCX,
)
class DocTitle(
    val texts: List<Text>
)
