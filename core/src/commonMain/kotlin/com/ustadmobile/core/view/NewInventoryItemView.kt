package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProduct

/**
 * Core View. Screen is for SaleList's View
 */
interface NewInventoryItemView : UstadView {

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
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProduct>)

    fun updateSortSpinner(sortPresets: Array<String?>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "NewInventoryItem"

        const val ARG_NEW_SALE_PRODUCT_UID = "ArgNewSaleProductUid"
    }

}

