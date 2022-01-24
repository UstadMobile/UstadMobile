package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson

interface SchoolMemberListView: UstadListView<SchoolMember, SchoolMemberWithPerson> {

    fun addMember()
    var pendingStudentList: DoorDataSourceFactory<Int, SchoolMemberWithPerson>?

    companion object {
        const val VIEW_NAME = "SchoolMemberListView"
    }

}