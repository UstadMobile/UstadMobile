package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable

@Serializable
data class Prf(
    val enabled: Boolean? = false
)