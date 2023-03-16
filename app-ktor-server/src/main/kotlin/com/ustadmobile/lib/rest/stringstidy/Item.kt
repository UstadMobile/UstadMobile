package com.ustadmobile.lib.rest.resmodel

import kotlinx.serialization.SerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@kotlinx.serialization.Serializable
@SerialName("item")
class Item(
    val quantity: String? = null,
    @XmlValue(true)
    val content: String?
) {
}