package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * Sale 's POJO for representing itself on the view (and recycler views)
 */
@Serializable
class SaleListDetail() : Sale() {

    //String categoryName;

    var locationName: String? = null
    var saleAmount: Float = 0.toFloat()
    var saleCurrency: String? = null
    var saleItemCount: Int = 0
    var saleTitleGen: String? = null
    var saleTitleGenDari: String? = null
    var saleTitleGenPashto: String? = null
    var saleProductNames: String? = null
    var saleProductNamesDari: String? = null
    var saleProductNamesPashto: String? = null
    var saleAmountPaid: Float = 0.toFloat()
    var saleAmountDue: Float = 0.toFloat()
    var earliestDueDate: Long = 0
    var saleItemPreOrder: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleListDetail

        if (locationName != other.locationName) return false
        if (saleAmount != other.saleAmount) return false
        if (saleCurrency != other.saleCurrency) return false
        if (saleItemCount != other.saleItemCount) return false
        if (saleTitleGen != other.saleTitleGen) return false
        if (saleTitleGenDari != other.saleTitleGenDari) return false
        if (saleTitleGenPashto != other.saleTitleGenPashto) return false
        if (saleProductNames != other.saleProductNames) return false
        if (saleProductNamesDari != other.saleProductNamesDari) return false
        if (saleProductNamesPashto != other.saleProductNamesPashto) return false
        if (saleAmountPaid != other.saleAmountPaid) return false
        if (saleAmountDue != other.saleAmountDue) return false
        if (earliestDueDate != other.earliestDueDate) return false
        if (saleItemPreOrder != other.saleItemPreOrder) return false
        if (salePreOrder != other.salePreOrder) return false
        if (salePaymentDone != other.salePaymentDone) return false
        return true
    }

    override fun hashCode(): Int {
        var result = locationName?.hashCode() ?: 0
        result = 31 * result + saleAmount.hashCode()
        result = 31 * result + (saleCurrency?.hashCode() ?: 0)
        result = 31 * result + saleItemCount
        result = 31 * result + (saleTitleGen?.hashCode() ?: 0)
        result = 31 * result + (saleTitleGenDari?.hashCode() ?: 0)
        result = 31 * result + (saleTitleGenPashto?.hashCode() ?: 0)
        result = 31 * result + (saleProductNames?.hashCode() ?: 0)
        result = 31 * result + (saleProductNamesDari?.hashCode() ?: 0)
        result = 31 * result + (saleProductNamesPashto?.hashCode() ?: 0)
        result = 31 * result + saleAmountPaid.hashCode()
        result = 31 * result + saleAmountDue.hashCode()
        result = 31 * result + earliestDueDate.hashCode()
        result = 31 * result + saleItemPreOrder.hashCode()
        return result
    }


}
