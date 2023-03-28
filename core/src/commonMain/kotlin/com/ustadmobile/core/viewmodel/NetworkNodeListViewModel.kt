package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ErrorReport


data class NetworkNodeListUiState(
    val errorReport: ErrorReport? = null,
    val fieldsEnabled: Boolean = true
)