package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
@Serializable
class SaleItemWithProduct() : SaleItem() {

    var deliveredCount = 0

    @Embedded
    var saleItemProduct: Product? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleItemWithProduct

        if (deliveredCount != other.deliveredCount) return false
        if (saleItemProduct != other.saleItemProduct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deliveredCount
        result = 31 * result + (saleItemProduct?.hashCode() ?: 0)
        return result
    }


}