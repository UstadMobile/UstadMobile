package com.ustadmobile.lib.rest.resmodel

import kotlinx.serialization.SerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@kotlinx.serialization.Serializable
@SerialName("string")
class ResString(
    val name: String,
    val formatted: String? = null,
    @XmlValue(true)
    val content: String?
) {
}
