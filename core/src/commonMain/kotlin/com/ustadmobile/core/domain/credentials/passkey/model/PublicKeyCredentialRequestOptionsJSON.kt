package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

//as per https://w3c.github.io/webauthn/#publickeycredential
@Serializable
class PublicKeyCredentialRequestOptionsJSON(
    val challenge: String,
    val timeout: Long? = null,
    val rpId: String? = null,
    val allowCredentials: List<PublicKeyCredentialDescriptorJSON> = emptyList(),
    val userVerification: String = "preferred",
    val hints: List<String> = emptyList(),
    val extensions: Map<String, String> = emptyMap()
)