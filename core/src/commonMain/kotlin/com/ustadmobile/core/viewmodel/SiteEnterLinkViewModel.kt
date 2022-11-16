package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Site

data class SiteEnterLinkUiState(
    var site: Site? = null,
    val validLink: Boolean = false,
    val progressVisible: Boolean = false,
    val linkError: String? = null,
    val fieldsEnabled: Boolean = true,
)