package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*

interface ProductDetailView: UstadDetailView<ProductWithInventoryCount> {

    var productCategories: DataSource.Factory<Int, Category>?
    var stockList: DataSource.Factory<Int, PersonWithInventoryCount>?
    var transactionList: DataSource.Factory<Int, InventoryTransactionDetail>?
    //TODO: Maybe product picture in the future
    var pictureList: DataSource.Factory<Int, Product>?

    companion object {

        const val VIEW_NAME = "ProductDetailView"

    }

}