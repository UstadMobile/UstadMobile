package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson

interface SchoolMemberListView: UstadListView<SchoolMember, SchoolMemberWithPerson> {

    fun addMember()

    companion object {
        const val VIEW_NAME = "SchoolMemberListView"
    }

}