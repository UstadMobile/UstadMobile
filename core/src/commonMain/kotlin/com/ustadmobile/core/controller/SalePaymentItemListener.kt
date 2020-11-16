package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SalePayment

interface SalePaymentItemListener {

    fun onClickSalePayment(salePayment: SalePayment)
    fun onClickRemoveSalePayment(salePayment: SalePayment)

}