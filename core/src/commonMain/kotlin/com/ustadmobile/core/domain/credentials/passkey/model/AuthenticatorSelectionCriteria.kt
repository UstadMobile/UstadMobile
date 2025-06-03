package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatorSelectionCriteria(
    val authenticatorAttachment: String,
    val residentKey: String,
    val requireResidentKey: Boolean = false,
    val userVerification: String = "preferred"
)
