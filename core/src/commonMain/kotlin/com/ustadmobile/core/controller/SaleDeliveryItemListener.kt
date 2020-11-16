package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleDelivery

interface SaleDeliveryItemListener {

    fun onClickSaleDelivery(saleDelivery: SaleDelivery)
    fun onClickRemoveSaleDelivery(saleDelivery: SaleDelivery)
}