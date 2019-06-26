package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SaleNameWithImage


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
    fun setRecentProvider(recentProvider: UmProvider<SaleNameWithImage>)

    fun setCategoryProvider(categoryProvider: UmProvider<SaleNameWithImage>)

    fun setCollectionProvider(collectionProvider: UmProvider<SaleNameWithImage>)

    fun showMessage(messageId: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectSaleProduct"
    }


}

