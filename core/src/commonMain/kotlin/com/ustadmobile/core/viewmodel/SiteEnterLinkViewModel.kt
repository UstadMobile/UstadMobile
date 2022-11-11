package com.ustadmobile.core.viewmodel

data class SiteEnterLinkUiState(
    val siteLink: String = "",
    val validLink: Boolean = false,
    val progressVisible: Boolean = false,
    val errorMessage: String = ""
)