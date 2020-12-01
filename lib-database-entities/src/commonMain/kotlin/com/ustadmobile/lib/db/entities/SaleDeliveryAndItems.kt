package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class SaleDeliveryAndItems() {

    @Embedded
    var delivery: SaleDelivery? = null
    var saleItems: List<SaleItemWithProduct> = listOf()

}