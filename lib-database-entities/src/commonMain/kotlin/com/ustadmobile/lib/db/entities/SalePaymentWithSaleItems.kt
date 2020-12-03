package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class SalePaymentWithSaleItems(
        var payment: SalePayment = SalePayment(),
        var saleDiscount: Long = 0L,
        var saleItems: List<SaleItemWithProduct> = listOf()
)