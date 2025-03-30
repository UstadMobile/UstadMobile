package com.ustadmobile.core.domain.credentials


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