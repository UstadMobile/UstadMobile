package com.ustadmobile.core.viewmodel.leavingreason.list

import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

data class LeavingReasonListUiState(

    val leavingReasonList: List<ClazzEnrolmentWithLeavingReason> = emptyList(),

)