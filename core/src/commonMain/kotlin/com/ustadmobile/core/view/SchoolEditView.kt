package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.School

/**
 * Core View. Screen is for SchoolDetail's View
 */
interface SchoolEditView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun setSchool(school: School)

    fun setPicture(picturePath: String)

    //Add any other view bits here..

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SchoolDetail"
        const val ARG_SCHOOL_DETAIL_SCHOOL_UID = "ArgSchoolDetailSchoolUid"
        const val ARG_SCHOOL_NEW = "ArgSchoolNew"
    }

}

