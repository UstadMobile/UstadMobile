package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseTerminology


interface CourseTerminologyListView: UstadListView<CourseTerminology, CourseTerminology> {

    companion object {
        const val VIEW_NAME = "CourseTerminologyListView"
    }

}