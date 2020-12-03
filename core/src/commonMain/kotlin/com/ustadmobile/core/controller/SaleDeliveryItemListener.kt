package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleDeliveryAndItems

interface SaleDeliveryItemListener {

    fun onClickSaleDelivery(saleDelivery: SaleDeliveryAndItems)
    fun onClickRemoveSaleDelivery(saleDelivery: SaleDeliveryAndItems)
}