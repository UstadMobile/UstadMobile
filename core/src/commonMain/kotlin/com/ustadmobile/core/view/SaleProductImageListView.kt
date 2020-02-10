package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProductPicture

/**
 * Core View. Screen is for SaleList's View
 */
interface SaleProductImageListView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProductPicture>)

    fun showGetImageAlertDialog()

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleProductImageList"

        const val ARG_MANAGE_IMAGES_SALE_PRODUCT_UID = "SaleProductImageListSaleProductUid"
    }
}

