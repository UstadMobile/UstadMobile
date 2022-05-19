package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseGroupSet


interface CourseGroupSetListView: UstadListView<CourseGroupSet, CourseGroupSet> {

    var individualList: List<CourseGroupSet>?

    companion object {
        const val VIEW_NAME = "CourseGroupSetListView"

        const val ARG_SHOW_INDIVIDUAL = "individualSubmission"
    }

}