package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable
import androidx.room.Embedded

@Serializable
class ProductDeliveryWithProductAndTransactions() : Product() {

    //Total items expected
    var numItemsExpected : Int = 0

    //The inventory item transactions for this person
    var transactions: List<PersonWithInventoryItemAndStock>? = listOf()

}