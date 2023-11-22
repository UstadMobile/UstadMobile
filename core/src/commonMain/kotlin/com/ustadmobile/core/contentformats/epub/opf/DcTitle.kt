package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_DC
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

/**
 * As per the Dublin Core Spec, all literal elements can occur zero or more times, e.g. to allow
 * multiple lang values (title in English, French, etc)
 */
@Serializable
@XmlSerialName(
    value = "title",
    namespace = NS_DC,
)
class DcTitle(
    @XmlSerialName(
        prefix = "xml"
    )
    val lang: String? = null,

    @XmlValue
    val content: String = ""
)
