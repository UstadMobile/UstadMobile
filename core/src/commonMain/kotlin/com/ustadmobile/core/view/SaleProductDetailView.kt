package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductSelected


/**
 * Core View. Screen is for SaleProductDetail's View
 */
interface SaleProductDetailView : UstadView {


    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProductSelected>)

    fun updateToolbarTitle(titleName: String)

    fun updateCategoryTitle(titleName: String)

    fun updateImageOnView(imagePath: String)

    fun addImageFromCamera()

    fun addImageFromGallery()

    fun initFromSaleProduct(saleProduct: SaleProduct, isNew : Boolean)

    fun sendMessage(messageId: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleProductDetail"

        //Any argument keys:
        const val ARG_NEW_TITLE = "ArgNewTitle"
        const val ARG_NEW_CATEGORY = "ArgNewCategory"
        const val ARG_SALE_PRODUCT_UID = "ArgSaleProductUid"
        const val ARG_ASSIGN_TO_CATEGORY_UID = "ArgAssignToCategoryUid"
        const val ARG_ADD_INVENTORY_POST_SAVE = "ArgAddInventoryPostSave"
    }

}

