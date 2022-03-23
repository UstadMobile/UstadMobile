package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseBlock

interface TextCourseBlockEditView: UstadEditView<CourseBlock> {

    var blockTitleError: String?

    var startDate: Long
    var startTime: Long

    var timeZone: String?

    companion object {

        const val VIEW_NAME = "TextCourseBlockEdit"
    }

}