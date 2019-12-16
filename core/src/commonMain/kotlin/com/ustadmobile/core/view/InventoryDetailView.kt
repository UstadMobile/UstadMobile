package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail


/**
 * Core View. Screen is for SaleList's View
 */
interface InventoryDetailView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, InventoryTransactionDetail>)

    fun updateTotalInventoryCount(count: Int)

    fun updateImageOnView(imagePath: String, skipCached: Boolean)

    fun updateToolbar(title: String)

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "InventoryDetail"

        const val ARG_INVENTORY_DETAIL_SALE_PRODUCT_UID = "ArgInventoryDetailSaleProductUid"
    }


}

