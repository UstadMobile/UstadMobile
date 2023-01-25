package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

data class ClazzEnrolmentListUiState(
    val enrolmentList: List<ClazzEnrolmentWithLeavingReason> = emptyList(),
    val personName: String? = null,
    var courseName: String? = null,
)