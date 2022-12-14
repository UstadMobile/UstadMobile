package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ErrorReport

data class ErrorReportUiState(
    val errorReport: ErrorReport? = null,
    val fieldsEnabled: Boolean = true
)