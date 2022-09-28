package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Person


interface ClazzEnrolmentListView: UstadListView<ClazzEnrolment, ClazzEnrolmentWithLeavingReason> {

    var person: Person?

    var clazz: Clazz?

    var enrolmentList: DataSourceFactory<Int, ClazzEnrolmentWithLeavingReason>?

    var isStudentEnrolmentEditVisible: Boolean

    var isTeacherEnrolmentEditVisible: Boolean

    companion object {
        const val VIEW_NAME = "CourseEnrolmentListView"
    }

}