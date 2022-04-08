package com.ustadmobile.core.view

import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet


interface CourseGroupSetEditView: UstadEditView<CourseGroupSet> {

    var memberList: List<CourseGroupMemberPerson>?

    var groupList: List<IdOption>?

    companion object {

        const val VIEW_NAME = "CourseGroupSetEditEditView"

    }

}