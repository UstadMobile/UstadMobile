package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemReminder


/**
 * Core View. Screen is for SaleItemDetail's View
 */
interface SaleItemDetailView : UstadView {


    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateSaleItemOnView(saleItem: SaleItem, productTitle: String)

    fun updateTotal(total: Long)

    fun updatePPP(ppp: Long)

    fun showPreOrder(show: Boolean)

    fun setReminderProvider(paymentProvider: UmProvider<SaleItemReminder>)

    fun setReminderNotification(days: Int, message: String, saleDueDate: Long)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleItemDetail"

        //Any argument keys:
        const val ARG_SALE_ITEM_UID = "ArgSaleItemUid"
        const val ARG_SALE_ITEM_PRODUCT_UID = "ArgSaleItemProducerUid"
        const val ARG_SALE_ITEM_DUE_DATE = "ArgSaleDueDate"

        const val ARG_SALE_ITEM_NAME = "ArgSaleItemName"
        const val ARG_SALE_DUE_DAYS = "ArgSaleDueDays"
    }

}

