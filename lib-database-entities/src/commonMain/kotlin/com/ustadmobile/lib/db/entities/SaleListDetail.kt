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

}
