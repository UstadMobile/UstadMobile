package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseBlock

interface ModuleCourseBlockEditView: UstadEditView<CourseBlock> {

    var blockTitleError: String?

    var startDate: Long
    var startTime: Long

    var timeZone: String?

    companion object {

        const val VIEW_NAME = "ModuleCourseBlockEdit"

    }

}