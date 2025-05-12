package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable

@Serializable
data class ClientExtensionResults(
    val prf: Prf? = null
)