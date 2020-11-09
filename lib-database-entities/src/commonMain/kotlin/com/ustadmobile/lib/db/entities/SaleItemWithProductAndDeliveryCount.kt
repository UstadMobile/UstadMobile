package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable
import androidx.room.Embedded

@Serializable
class SaleItemWithProductAndDeliveryCount() : SaleItem() {

    var deliveredCount = 0

    @Embedded
    var saleItemSaleProduct: Product? = null

}

