package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.LeavingReason


interface LeavingReasonEditView: UstadEditView<LeavingReason> {

    var reasonTitleError: String?

    companion object {

        const val VIEW_NAME = "LeavingReasonEditEditView"

    }

}