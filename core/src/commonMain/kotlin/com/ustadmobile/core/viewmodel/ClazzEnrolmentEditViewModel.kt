package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

data class ClazzEnrolmentEditUiState(

    val clazzEnrolment: ClazzEnrolmentWithLeavingReason? = null,

    val roleSelectedError: String? = null,

    val fieldsEnabled: Boolean = true,
)