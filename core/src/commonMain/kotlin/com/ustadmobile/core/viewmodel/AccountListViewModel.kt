package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint

data class AccountListUiState(
    val activeAccount: UserSessionWithPersonAndEndpoint? = null,
    val accountsList: List<UserSessionWithPersonAndEndpoint> = emptyList(),
    val version: String = "Version 0.2.1 'KittyHawk'",
)