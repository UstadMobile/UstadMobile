package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet


interface CourseGroupSetDetailView: UstadDetailView<CourseGroupSet> {

    var memberList: List<CourseGroupMemberPerson>?

    companion object {

        const val VIEW_NAME = "CourseGroupSetDetailView"

    }

}