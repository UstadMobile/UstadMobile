package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseGroupSet


interface CourseGroupSetListView: UstadListView<CourseGroupSet, CourseGroupSet> {

    companion object {
        const val VIEW_NAME = "CourseGroupSetListView"
    }

}