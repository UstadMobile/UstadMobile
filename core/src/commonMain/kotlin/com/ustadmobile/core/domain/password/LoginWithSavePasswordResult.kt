package com.ustadmobile.core.domain.password

data class LoginWithSavePasswordResult(
    val username: String? = null,
    val password: String? = null,
    val error: String? = null,
)