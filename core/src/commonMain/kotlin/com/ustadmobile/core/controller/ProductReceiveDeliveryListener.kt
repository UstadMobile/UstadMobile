package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ProductDeliveryWithProductAndTransactions
import com.ustadmobile.lib.db.entities.SaleItem

interface ProductReceiveDeliveryListener {

    fun onClickProductForDelivery(saleItem: ProductDeliveryWithProductAndTransactions)

}