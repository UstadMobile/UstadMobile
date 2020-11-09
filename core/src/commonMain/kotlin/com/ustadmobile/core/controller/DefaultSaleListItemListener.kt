package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail

class DefaultSaleListItemListener(var view: SaleListView?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): SaleListItemListener {

    override fun onClickSale(sale: SaleListDetail) {
        if(listViewMode == ListViewMode.BROWSER) {
            //TODO
//            systemImpl.go(SaleDetailView.VIEW_NAME,
//                    mapOf(UstadView.ARG_ENTITY_UID to sale.saleUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(sale))
        }
    }
}
