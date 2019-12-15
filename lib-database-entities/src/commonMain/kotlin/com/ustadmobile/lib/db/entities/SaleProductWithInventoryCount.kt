package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SaleProductWithInventoryCount : SaleProduct() {
    var stock: Int = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleProductWithInventoryCount

        if (stock != other.stock) return false
        if(saleProductName != other.saleProductName) return false
        if(saleProductNameDari != other.saleProductNameDari) return false
        if(saleProductNamePashto != other.saleProductNamePashto) return false
        if(saleProductDesc != other.saleProductDesc) return false
        if(saleProductDescDari != other.saleProductDescDari) return false
        if(saleProductDescPashto != other.saleProductDescPashto) return false
        if(saleProductUid != other.saleProductUid) return false

        return true
    }

    override fun hashCode(): Int {
        return stock
    }


}
