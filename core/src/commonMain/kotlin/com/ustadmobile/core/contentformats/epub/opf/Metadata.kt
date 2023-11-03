package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "metadata",
    namespace = NS_OPF,
)
@Serializable
class Metadata(

    val titles: List<DcTitle> = emptyList(),

    val creators: List<DcCreator> = emptyList(),

    val languages: List<DcLanguage> = emptyList(),

    val descriptions: List<DcDescription> = emptyList(),

    val identifiers: List<DcIdentifier> = emptyList(),

)
