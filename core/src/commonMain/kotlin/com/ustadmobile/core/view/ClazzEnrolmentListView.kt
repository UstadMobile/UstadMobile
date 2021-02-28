package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Person


interface ClazzEnrolmentListView: UstadListView<ClazzEnrolment, ClazzEnrolmentWithLeavingReason> {

    var person: Person?

    var clazz: Clazz?

    var enrolmentList: DataSource.Factory<Int, ClazzEnrolmentWithLeavingReason>?

    var isStudentEnrolmentEditVisible: Boolean

    var isTeacherEnrolmentEditVisible: Boolean

    companion object {
        const val VIEW_NAME = "ClazzEnrolmentListView"
    }

}