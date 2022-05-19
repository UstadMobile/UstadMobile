package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails

interface ClazzMemberListView: UstadListView<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails> {

    var studentList: DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails>?

    var pendingStudentList: DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails>?

    var addTeacherVisible: Boolean

    var addStudentVisible: Boolean

    var termMap: Map<String, String>?

    companion object {

        const val ARG_HIDE_CLAZZES = "hideClazzes"

        const val VIEW_NAME = "CourseMemberListView"
    }

}