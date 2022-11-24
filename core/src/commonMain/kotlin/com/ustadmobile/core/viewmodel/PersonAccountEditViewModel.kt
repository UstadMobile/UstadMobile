package com.ustadmobile.core.viewmodel

data class PersonUsernameAndPasswordModel(

    val username: String = "",

    val currentPassword: String = "",

    val newPassword: String = "",

    val passwordConfirmed: String = "",
)

data class PersonAccountEditUiState(

    val personUsernameAndPassword: PersonUsernameAndPasswordModel = PersonUsernameAndPasswordModel(),

    val usernameError: String? = null,

    val currentPasswordError: String? = null,

    val newPasswordError: String? = null,

    val passwordConfirmedError: String? = null,

    val errorMessage: String? = null,

    val currentPasswordVisible: Boolean = false,

    val usernameVisible: Boolean = false,

    val fieldsEnabled: Boolean = true,
)