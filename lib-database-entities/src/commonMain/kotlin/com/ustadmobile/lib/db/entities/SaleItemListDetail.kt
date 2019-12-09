package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
@Serializable
class SaleItemListDetail() : SaleItem() {

    var saleItemPictureUid: Long = 0
    var saleItemProductName: String ?= null

    @Embedded
    var saleItemSaleProduct: SaleProduct? = null

}
