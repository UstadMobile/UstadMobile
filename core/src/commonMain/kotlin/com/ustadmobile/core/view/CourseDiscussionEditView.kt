package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

interface CourseDiscussionEditView: UstadEditView<CourseBlockWithEntity> {

    var blockTitleError: String?

    var startDate: Long
    var startTime: Long

    var timeZone: String?

    companion object {

        const val VIEW_NAME = "CourseDiscussionBlockEdit"

    }

}