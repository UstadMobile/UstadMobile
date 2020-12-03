package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems

interface SalePaymentItemListener {

    fun onClickSalePayment(salePayment: SalePaymentWithSaleItems)
    fun onClickRemoveSalePayment(salePayment: SalePaymentWithSaleItems)

}