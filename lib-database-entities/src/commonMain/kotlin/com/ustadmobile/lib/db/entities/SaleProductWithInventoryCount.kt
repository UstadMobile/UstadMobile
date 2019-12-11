package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SaleProductWithInventoryCount : SaleProduct() {
    var stock: Int = 0
}
