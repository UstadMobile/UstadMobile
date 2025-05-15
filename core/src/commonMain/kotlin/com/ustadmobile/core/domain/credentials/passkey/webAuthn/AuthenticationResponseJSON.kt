package com.ustadmobile.core.domain.credentials.passkey.webAuthn

import kotlinx.serialization.Serializable
//during registration and signin with passkey the standard webAuthN response
// received as per https://w3c.github.io/webauthn/#dictdef-authenticationresponsejson
@Serializable
data class AuthenticationResponseJSON(
    val id: String,
    val rawId: String,
    val response: AuthenticatorAssertionResponseJSON,
    val authenticatorAttachment: String?=null,
    val clientExtensionResults: AuthenticationExtensionsClientOutputsJSON,
    val type: String
)


