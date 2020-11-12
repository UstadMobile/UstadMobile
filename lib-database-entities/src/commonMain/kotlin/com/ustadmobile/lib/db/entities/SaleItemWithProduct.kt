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
    var saleItemSaleProduct: SaleProduct? = null

}