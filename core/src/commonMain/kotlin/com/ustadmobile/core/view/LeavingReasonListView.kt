package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.LeavingReason


interface LeavingReasonListView: UstadListView<LeavingReason, LeavingReason> {

    companion object {
        const val VIEW_NAME = "LeavingReasonListView"
    }

}