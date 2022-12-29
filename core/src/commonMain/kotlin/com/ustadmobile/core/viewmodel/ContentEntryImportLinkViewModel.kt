package com.ustadmobile.core.viewmodel

data class ContentEntryImportLinkUiState(
    var url: String? = null,
    val linkError: String? = null,
    val fieldsEnabled: Boolean = true
)