package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProduct

/**
 * Core View. Screen is for AddSaleProductToSaleCategory's View
 */
interface AddSaleProductToSaleCategoryView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Product list
     * @param listProvider SameNameWithImage product provider
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProduct>)

    fun setAddtitle(title: String)

    fun setToolbarTitle(title: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "AddSaleProductToSaleCategory"

        //Any argument keys:
        const val ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID = "ArgSaleProductCategoryToAssignToUid"
        const val ARG_ADD_TO_CATEGORY_TYPE_ITEM = "ArgAddToCategoryTypeItem"
        const val ARG_ADD_TO_CATEGORY_TYPE_CATEGORY = "argAddToCategoryTypeCategory"
    }
}

