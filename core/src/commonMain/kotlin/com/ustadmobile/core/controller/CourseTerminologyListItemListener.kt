package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.CourseTerminology


interface CourseTerminologyListItemListener {

    fun onClickCourseTerminology(courseTerminology: CourseTerminology)

}