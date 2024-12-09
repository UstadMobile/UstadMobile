package com.ustadmobile.core.domain.passkey


data class PassKeySignInData(
    val credentialId: String,
    val userHandle: String,
    val authenticatorData: String,
    val clientDataJSON: String,
    val signature: String,
    val origin: String,
    val rpId: String,
    val challenge: String
)