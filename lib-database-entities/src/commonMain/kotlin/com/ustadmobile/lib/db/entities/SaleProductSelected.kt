package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SaleProductSelected : SaleProduct() {

    var isSelected: Boolean = false
}
