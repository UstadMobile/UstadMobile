package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.SaleProduct

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectMultipleProductTypeTreeDialogView : UstadView {

    fun populateTopProductType(productTypes: List<SaleProduct>)

    fun setTitle(title: String)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectMultipleProductTypeTreeDialog"

        val ARG_PRODUCT_SELECTED_SET = "ProductSelected"
    }

}
