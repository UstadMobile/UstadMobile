package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.LeavingReason

data class LeavingReasonEditUiState(
    val leavingReason: LeavingReason? = null,
    val reasonTitleError: String? = null,
    val fieldsEnabled: Boolean = true,
)