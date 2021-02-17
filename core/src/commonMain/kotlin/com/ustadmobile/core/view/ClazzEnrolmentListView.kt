package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Person


interface ClazzEnrolmentListView: UstadListView<ClazzEnrolment, ClazzEnrolmentWithLeavingReason> {

    var person: Person?

    companion object {
        const val VIEW_NAME = "ClazzEnrolmentListView"
    }

}