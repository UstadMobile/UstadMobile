package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail

interface SaleListItemListener {

    fun onClickSale(sale: SaleListDetail)

}