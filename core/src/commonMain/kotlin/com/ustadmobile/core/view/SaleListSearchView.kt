package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SaleListDetail


/**
 * Core View. Screen is for SaleListSearch's View
 */
interface SaleListSearchView : UstadView {

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
    fun setListProvider(listProvider: UmProvider<SaleListDetail>)

    fun updateLocationSpinner(locations: Array<String>)

    fun updateDateRangeText(dateRangeText: String)

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    fun updateSortSpinner(presets: Array<String?>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleListSearch"

        const val SORT_MOST_RECENT = 1
        const val SORT_LOWEST_PRICE = 2
        const val SORT_HIGHEST_PRICE = 3
    }

}

