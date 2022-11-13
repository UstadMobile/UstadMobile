package com.ustadmobile.core.viewmodel

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameEnabled: Boolean = true,
    val passwordEnabled: Boolean = true,
    val isEmptyPassword: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val versionInfo: String = "v42",
    val isEmptyUsername: Boolean = false,
    val inProgress: Boolean = false,
    val createAccountVisible: Boolean = false,
    val connectAsGuestVisible: Boolean = false,
    val loginIntentMessage: String = ""
)