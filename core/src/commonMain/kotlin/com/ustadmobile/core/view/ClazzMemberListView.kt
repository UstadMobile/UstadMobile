package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson

interface ClazzMemberListView: UstadListView<ClazzMember, ClazzMemberWithPerson> {

    var studentList: DataSource.Factory<Int, ClazzMemberWithPerson>?

    var addTeacherVisible: Boolean

    var addStudentVisible: Boolean

    companion object {
        const val VIEW_NAME = "ClazzMemberListView"
    }

}