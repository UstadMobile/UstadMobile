package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class SaleDeliveryAndItems() : SaleDelivery() {

    var saleItems: List<SaleItemWithProduct> = listOf()

    var deliveryDetails: List<ProductDeliveryWithProductAndTransactions> = mutableListOf()

    fun createWithDelivery(saleDelivery: SaleDelivery){
        saleDeliveryUid = saleDelivery.saleDeliveryUid
        saleDeliverySaleUid = saleDelivery.saleDeliverySaleUid
        saleDeliverySignature = saleDelivery.saleDeliverySignature
        saleDeliveryPersonUid = saleDelivery.saleDeliveryPersonUid
        saleDeliveryDate = saleDelivery.saleDeliveryDate
        saleDeliveryActive = saleDelivery.saleDeliveryActive
        saleDeliveryUid = saleDelivery.saleDeliveryUid
        saleDeliveryUid = saleDelivery.saleDeliveryUid

    }

}