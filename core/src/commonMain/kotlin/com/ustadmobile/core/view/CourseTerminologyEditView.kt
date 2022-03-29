package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseTerminologyWithLabel


interface CourseTerminologyEditView: UstadEditView<CourseTerminologyWithLabel> {

    var titleErrorText: String?

    var teacherErrorText: String?

    var studentErrorText: String?


    var addTeacherErrorText: String?

    var addStudentErrorText: String?


    companion object {

        const val VIEW_NAME = "CourseTerminologyEditEditView"

    }

}