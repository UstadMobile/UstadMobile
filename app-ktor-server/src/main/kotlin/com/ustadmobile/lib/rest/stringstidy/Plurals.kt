package com.ustadmobile.lib.rest.resmodel

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
@SerialName("plurals")
class Plurals(
    val name: String,
    val items: List<Item>,
) {
}