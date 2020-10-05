package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.LearnerGroupMemberWithPerson

interface LearnerGroupMemberListView : UstadListView<LearnerGroupMember, LearnerGroupMemberWithPerson> {


    companion object {

        const val VIEW_NAME = "LearnerGroupMemberList"
    }


}