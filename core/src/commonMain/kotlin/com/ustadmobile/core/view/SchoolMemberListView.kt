package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson

interface SchoolMemberListView: UstadListView<SchoolMember, SchoolMemberWithPerson> {

    fun addMember()
    var pendingStudentList: DataSource.Factory<Int, SchoolMemberWithPerson>?

    companion object {
        const val VIEW_NAME = "SchoolMemberListView"
    }

}