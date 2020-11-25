package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
@Serializable
class SaleItemWithProductAndPersonWithInventoryCount() : SaleItem() {

    //Total inventory count
    var inventoryCount = -1

    var inventoryCountDeliveredTotal = -1

    var inventoryCountDelivered = -1

    //Inventory selected
    var inventorySelected = -1

    @Embedded
    var producer: Person? = null

    @Embedded
    var saleItemProduct: Product? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleItemWithProductAndPersonWithInventoryCount

        if (inventoryCount != other.inventoryCount) return false
        if (inventoryCountDeliveredTotal != other.inventoryCountDeliveredTotal) return false
        if (inventoryCountDelivered != other.inventoryCountDelivered) return false
        if (inventorySelected != other.inventorySelected) return false
        if (producer != other.producer) return false
        if (saleItemProduct != other.saleItemProduct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inventoryCount
        result = 31 * result + inventoryCountDeliveredTotal
        result = 31 * result + inventoryCountDelivered
        result = 31 * result + inventorySelected
        result = 31 * result + (producer?.hashCode() ?: 0)
        result = 31 * result + (saleItemProduct?.hashCode() ?: 0)
        return result
    }


}