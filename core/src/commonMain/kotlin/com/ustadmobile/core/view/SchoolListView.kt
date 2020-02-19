package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.School

/**
 * Core View. Screen is for SaleList's View
 */
interface SchoolListView : UstadView {
    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, School>)

    fun setSortOptions(presets: Array<String>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SchoolList"
    }

}

