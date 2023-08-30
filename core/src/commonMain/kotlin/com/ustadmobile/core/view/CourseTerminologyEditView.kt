package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.core.impl.locale.TerminologyEntry


interface CourseTerminologyEditView: UstadEditView<CourseTerminology> {

    var titleErrorText: String?

    var terminologyTermList: List<TerminologyEntry>?

    companion object {

        const val VIEW_NAME = "CourseTerminologyEditView"

    }

}