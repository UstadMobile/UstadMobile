package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithPerson

interface ClazzMemberListView: UstadListView<ClazzEnrollment, ClazzEnrollmentWithPerson> {

    var studentList: DataSource.Factory<Int, ClazzEnrollmentWithPerson>?

    var pendingStudentList: DataSource.Factory<Int, ClazzEnrollmentWithPerson>?

    var addTeacherVisible: Boolean

    var addStudentVisible: Boolean

    companion object {
        const val VIEW_NAME = "ClazzMemberListView"
    }

}