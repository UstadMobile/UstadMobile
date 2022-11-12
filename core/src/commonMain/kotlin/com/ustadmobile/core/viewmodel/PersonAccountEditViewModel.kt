package com.ustadmobile.core.viewmodel

data class PersonUsernameAndPasswordModel(

    val username: String = "",

    val currentPassword: String = "",

    val newPassword: String = "",

    val passwordConfirmed: String = "",
)

data class PersonAccountEditUiState(

    val personUsernameAndPassword: PersonUsernameAndPasswordModel = PersonUsernameAndPasswordModel(),

    val currentPasswordError: String = "",
)