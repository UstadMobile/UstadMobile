package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SaleProduct


/**
 * Core View. Screen is for SaleProductCategoryList's View
 */
interface SaleProductCategoryListView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Recycler view for Items
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, SaleProduct>, allMode:Boolean)

    /**
     * Recycler view for Categories
     * @param listProvider
     */
    fun setCategoriesListProvider(listProvider: DataSource.Factory<Int, SaleProduct>, allMode:Boolean)

    fun setMessageOnView(messageCode: Int)

    //eg: set toolbar
    fun initFromSaleCategory(saleProductCategory: SaleProduct)

    fun updateSortPresets(presets: Array<String?>)

    fun hideFAB(hide: Boolean)

    fun hideEditMenu(hide: Boolean)

    fun updateToolbar(title: String?)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleProductCategoryList"

        //Any argument keys:
        const val ARG_SALEPRODUCT_UID = "ArgSaleProductUid"
        const val ARG_SELECT_PRODUCT = "ArgSelectProductMode"
        const val ARG_PASS_PRODUCER_UID = "ArgPassProducerUid"
        const val ARG_PASS_SALE_ITEM_UID = "ArgPassSaleItemUid"
        const val ARG_MORE_RECENT = "ArgMoreRecent"
        const val ARG_MORE_CATEGORY = "ArgMoreCategory"
    }

}

