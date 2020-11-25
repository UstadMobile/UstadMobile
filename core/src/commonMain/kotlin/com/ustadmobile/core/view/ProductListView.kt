package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

interface ProductListView: UstadListView<Product, ProductWithInventoryCount> {

    fun goToSaleItem(product: Product)

    companion object {
        const val VIEW_NAME = "ProductListView"
    }

}