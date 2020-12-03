package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class SaleDeliveryAndItems(
        var delivery: SaleDelivery = SaleDelivery(),
        var saleItems: List<SaleItemWithProduct> = listOf(),
        var deliveryDetails: List<ProductDeliveryWithProductAndTransactions> = mutableListOf(),
        var payments: List<SalePaymentWithSaleItems> = mutableListOf()
)