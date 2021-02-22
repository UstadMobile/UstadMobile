package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails

interface ClazzMemberListView: UstadListView<PersonWithClazzEnrolmentDetails, PersonWithClazzEnrolmentDetails> {

    var studentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>?

    var pendingStudentList: DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>?

    var addTeacherVisible: Boolean

    var addStudentVisible: Boolean

    companion object {
        const val VIEW_NAME = "ClazzMemberListView"
    }

}