package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.ProductDetailView
import com.ustadmobile.core.view.ProductListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

class DefaultProductListItemListener(var view: ProductListView?,
                                   var listViewMode: ListViewMode,
                                   val systemImpl: UstadMobileSystemImpl,
                                   val context: Any): ProductListItemListener {

    override fun onClickProduct(product: ProductWithInventoryCount) {
        if(listViewMode == ListViewMode.BROWSER) {
            //TODO
//            systemImpl.go(ProductDetailView.VIEW_NAME,
//                    mapOf(UstadView.ARG_ENTITY_UID to product.productUid.toString()), context)
        }else {
            view?.finishWithResult(listOf(product))
        }
    }
}
