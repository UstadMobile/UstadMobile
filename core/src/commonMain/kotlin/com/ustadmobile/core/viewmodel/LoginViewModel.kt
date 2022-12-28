package com.ustadmobile.core.viewmodel

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val fieldsEnabled: Boolean = true,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val versionInfo: String = "v42",
    val createAccountVisible: Boolean = false,
    val connectAsGuestVisible: Boolean = false,
    val loginIntentMessage: String? = null,
    val errorMessage: String? = null
)