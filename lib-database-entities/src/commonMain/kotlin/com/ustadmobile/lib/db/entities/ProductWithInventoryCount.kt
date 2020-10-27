package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ProductWithInventoryCount : Product() {
    var stock: Int = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ProductWithInventoryCount

        if (stock != other.stock) return false
        if(productName != other.productName) return false
        if(productNameDari != other.productNameDari) return false
        if(productNamePashto != other.productNamePashto) return false
        if(productDesc != other.productDesc) return false
        if(productDescDari != other.productDescDari) return false
        if(productDescPashto != other.productDescPashto) return false
        if(productUid != other.productUid) return false

        return true
    }

    override fun hashCode(): Int {
        return stock
    }


}
