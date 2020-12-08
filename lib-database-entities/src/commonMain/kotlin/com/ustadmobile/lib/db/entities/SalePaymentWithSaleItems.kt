package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class SalePaymentWithSaleItems() : SalePayment() {
    var saleDiscount: Long = 0L
    var saleItems: List<SaleItemWithProduct> = listOf()

    fun createWithPayment(salePayment: SalePayment){

        salePaymentUid = salePayment.salePaymentUid
        salePaymentPaidDate = salePayment.salePaymentPaidDate
        salePaymentPaidAmount = salePayment.salePaymentPaidAmount
        salePaymentCurrency = salePayment.salePaymentCurrency
        salePaymentSaleUid = salePayment.salePaymentSaleUid
        salePaymentActive = salePayment.salePaymentActive

    }
}