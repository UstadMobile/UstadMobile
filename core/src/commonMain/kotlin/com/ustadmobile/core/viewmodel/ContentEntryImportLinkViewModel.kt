package com.ustadmobile.core.viewmodel

import io.ktor.http.*

data class ContentEntryImportLinkUiState(
    var url: Url? = null,
    val linkError: String? = null,
    val fieldsEnabled: Boolean = true
)