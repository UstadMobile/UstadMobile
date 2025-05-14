package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable
//during registration and signin with passkey the standard webAuthN response
// received as per https://w3c.github.io/webauthn/#dictdef-registrationresponsejson
@Serializable
data class PasskeyWebAuthNResponse(
    val authenticatorAttachment: String? = null,
    val clientExtensionResults: ClientExtensionResults? = null,
    val id: String,
    val rawId: String? = null,
    val response: Response,
    val type: String? = null
)


