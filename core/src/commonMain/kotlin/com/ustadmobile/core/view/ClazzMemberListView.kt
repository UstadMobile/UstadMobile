package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails

interface ClazzMemberListView: UstadListView<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails> {

    var studentList: DataSourceFactory<Int, PersonWithClazzEnrolmentDetails>?

    var pendingStudentList: DataSourceFactory<Int, PersonWithClazzEnrolmentDetails>?

    var addTeacherVisible: Boolean

    var addStudentVisible: Boolean

    var termMap: Map<String, String>?

    companion object {

        const val ARG_HIDE_CLAZZES = "hideClazzes"

        const val VIEW_NAME = "CourseMemberListView"
    }

}