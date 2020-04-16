package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.School

/**
 * Core View. Screen is for SchoolDetail's View
 */
interface SchoolDetailView : UstadView {

    //Updates school name on view and sets edit gear icon to visible and feature visibility
    fun setSchool(school: School)

    fun setSettingsVisible(visible: Boolean)

    fun setUpTabs(tabs: List<String>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SchoolDetail"
    }

}

