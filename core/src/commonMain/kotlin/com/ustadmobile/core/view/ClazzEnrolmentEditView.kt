package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason


interface ClazzEnrolmentEditView: UstadEditView<ClazzEnrolmentWithLeavingReason> {


    var roleList: List<IdOption>?
    var statusList: List<IdOption>?

    var startDateError: Pair<String, Long>?
    var endDateError: String?
    var roleSelectionError: String?

    companion object {

        const val VIEW_NAME = "ClazzEnrolmentEditView"

    }

}