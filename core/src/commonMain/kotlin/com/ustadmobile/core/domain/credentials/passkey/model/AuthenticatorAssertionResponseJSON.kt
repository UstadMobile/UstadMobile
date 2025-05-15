package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticatorAssertionResponseJSON(
    val attestationObject: String?=null,
    val authenticatorData: String,
    val clientDataJSON: String,
    val publicKey: String?=null,
    val userHandle: String?=null,
    val signature: String?=null
)
