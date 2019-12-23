package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SalePayment


/**
 * Core View. Screen is for SalePaymentDetail's View
 */
interface SalePaymentDetailView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateSalePaymentOnView(payment: SalePayment)

    fun updateDefaultValue(value: Long)

    fun updateMaxValue(value: Long)

    fun updateMaxPaymentValue(value: Long)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SalePaymentDetail"

        //Any argument keys:
        const val ARG_SALE_PAYMENT_UID = "ArgSalePaymentUid"
        const val ARG_SALE_PAYMENT_DEFAULT_VALUE = "ArgSalePaymentDefaultValue"
    }

}

