package com.ustadmobile.lib.rest.resmodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
@SerialName("string-array")
class StringArray(
    val name: String,
    val items: List<Item>
) {

}