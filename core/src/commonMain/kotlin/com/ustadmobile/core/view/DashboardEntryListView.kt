package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.DashboardEntry
import com.ustadmobile.lib.db.entities.DashboardTag


/**
 * Core View. Screen is for DashboardEntryList's View
 */
interface DashboardEntryListView : UstadView {

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
    fun setDashboardEntryProvider(listProvider: DataSource.Factory<Int, DashboardEntry>)

    fun setDashboardTagProvider(listProvider: DataSource.Factory<Int, DashboardTag>)

    fun loadChips(tags: Array<String>)

    fun showSetTitle(existingTitle: String, entryUid: Long)

    fun showSalesLogOption(show: Boolean)
    fun showTopLEsOption(show: Boolean)
    fun showSalesPerformanceOption(show: Boolean)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "DashboardEntryList"
    }

}

