package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Language

data class LanguageEditUiState(
    val language: Language? = null,
    val fieldsEnabled: Boolean = true,
    val languageNameError: String? = null,
    val twoLettersCodeError: String? = null,
    val threeLettersCodeError: String? = null
)