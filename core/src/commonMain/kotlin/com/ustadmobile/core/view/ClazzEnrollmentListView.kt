package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.Person


interface ClazzEnrollmentListView: UstadListView<ClazzEnrollment, ClazzEnrollment> {

    var person: Person?

    companion object {
        const val VIEW_NAME = "ClazzEnrollmentListView"
    }

}