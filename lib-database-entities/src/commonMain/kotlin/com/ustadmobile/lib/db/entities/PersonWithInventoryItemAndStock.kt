package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable
import androidx.room.Embedded

@Serializable
class PersonWithInventoryItemAndStock() : Person() {

    //Most usually the producer/ W.E.
    @Embedded
    var inventoryItem: InventoryItem? = InventoryItem()

    //Total inventory/stock count that This person has
    var stock: Int = 0

    var selectedStock: Int = 0

}