package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val attestationObject: String?=null,
    val authenticatorData: String? = null,
    val clientDataJSON: String,
    val publicKey: String?=null,
    val publicKeyAlgorithm: Int? = 0,
    val transports: List<String>? = null,
    val userHandle: String? = null,
    val signature: String? = null
)
