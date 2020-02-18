package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.School

/**
 * Core View. Screen is for SchoolDetail's View
 */
interface SchoolDetailView : UstadView {
    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, School>)

    //Updates school name on view and sets edit gear icon to visible and feature visibility
    fun updateSchoolOnView(school: School)

    fun setupViewPager()

    //If we are keeping attendance as a feature
    fun setAttendanceVisibility(visible: Boolean)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SchoolDetail"
    }

}

