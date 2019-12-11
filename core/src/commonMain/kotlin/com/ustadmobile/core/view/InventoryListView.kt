package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProductWithInventoryCount


/**
 * Core View. Screen is for SaleList's View
 */
interface InventoryListView : UstadView {

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
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProductWithInventoryCount>)


    fun updateSortSpinner(presets: Array<String?>)

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "InventoryList"
    }


}

