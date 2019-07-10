package com.ustadmobile.lib.db.entities

/**
 * Sale 's POJO for representing itself on the view (and recycler views)
 */
class SaleListDetail() : Sale() {

    //String categoryName;

    var locationName: String? = null
    var saleAmount: Float = 0.toFloat()
    var saleCurrency: String? = null
    var saleItemCount: Int = 0
    var saleTitleGen: String? = null
    var saleProductNames: String? = null
    var saleAmountPaid: Float = 0.toFloat()
    var saleAmountDue: Float = 0.toFloat()
    var earliestDueDate: Long = 0
    var saleItemPreOrder: Boolean = false

}
