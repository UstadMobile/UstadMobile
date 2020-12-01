package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable
import androidx.room.Embedded

@Serializable
class ProductDeliveryWithProductAndTransactions() : Product() {

    @Embedded
    var saleDelivery: SaleDelivery? = null

    //Total items expected
    var items : Int = 0

    //The inventory item transactions for this person
    var transactions: List<PersonWithInventoryItemAndStock>? = listOf()

}