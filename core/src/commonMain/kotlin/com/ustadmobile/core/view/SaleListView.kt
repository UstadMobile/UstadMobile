package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SaleListDetail


/**
 * Core View. Screen is for SaleList's View
 */
interface SaleListView : UstadView {

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
    fun setListProvider(listProvider: UmProvider<SaleListDetail>, paymentsDueTab: Boolean, preOrderTab: Boolean)


    fun updateSortSpinner(presets: Array<String?>)

    fun updatePreOrderCounter(count: Int)

    fun updatePaymentDueCounter(count: Int)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleList"
    }


}

