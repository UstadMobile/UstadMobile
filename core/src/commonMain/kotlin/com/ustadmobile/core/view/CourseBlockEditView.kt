package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseBlock

interface CourseBlockEditView: UstadEditView<CourseBlock> {

    var blockTitleError: String?

    companion object {

        const val VIEW_NAME = "CourseBlockEdit"
    }

}