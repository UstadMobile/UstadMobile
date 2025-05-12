package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyWebAuthNResponse(
    val authenticatorAttachment: String? = null,
    val clientExtensionResults: ClientExtensionResults? = null,
    val id: String,
    val rawId: String? = null,
    val response: Response,
    val type: String? = null
)


