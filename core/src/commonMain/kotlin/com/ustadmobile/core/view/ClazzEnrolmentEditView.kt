package com.ustadmobile.core.view

import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason


interface ClazzEnrolmentEditView: UstadEditView<ClazzEnrolmentWithLeavingReason> {


    var roleList: List<IdOption>?
    var statusList: List<IdOption>?

    var startDateErrorWithDate: Pair<String, Long>?
    var endDateError: String?
    var roleSelectionError: String?

    companion object {

        const val VIEW_NAME = "CourseEnrolmentEditView"

    }

}