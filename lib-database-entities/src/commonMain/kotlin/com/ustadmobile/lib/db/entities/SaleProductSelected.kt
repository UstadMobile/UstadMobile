package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SaleProductSelected : SaleProduct() {

    var isSelected: Boolean = false


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as SaleProductSelected

        if (isSelected != other.isSelected) return false
        if (saleProductUid != other.saleProductUid) return false
        if (saleProductName != other.saleProductName) return false
        if (saleProductNameDari != other.saleProductNameDari) return false
        if (saleProductNamePashto != other.saleProductNamePashto) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }


}
