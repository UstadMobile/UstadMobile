package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProduct


/**
 * Core View. Screen is for SelectSaleProduct's View
 */
interface SelectSaleProductView : UstadView {

    //Any argument keys:


    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param recentProvider The provider to set to the view
     */
    fun setRecentProvider(recentProvider: DataSource.Factory<Int, SaleProduct>)

    fun setCategoryProvider(categoryProvider: DataSource.Factory<Int, SaleProduct>)

    fun setCollectionProvider(collectionProvider: DataSource.Factory<Int, SaleProduct>)

    fun showMessage(messageId: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectSaleProduct"
    }


}

