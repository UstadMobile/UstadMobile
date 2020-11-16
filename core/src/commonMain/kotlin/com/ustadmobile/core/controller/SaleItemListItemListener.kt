package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleItem

interface SaleItemListItemListener {

    fun onClickSaleItem(saleItem: SaleItem)

}