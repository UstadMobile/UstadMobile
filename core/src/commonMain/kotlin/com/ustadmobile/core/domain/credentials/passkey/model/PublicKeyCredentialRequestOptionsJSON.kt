package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable
//as per https://w3c.github.io/webauthn/#dictdef-publickeycredentialrequestoptionsjson
@Serializable
class PublicKeyCredentialRequestOptionsJSON(
    val challenge: String,
    val timeout: Long? = null,
    val rpId: String? = null,
    val allowCredentials: List<PublicKeyCredentialDescriptorJSON> = emptyList(),
    val userVerification: String? = null,
    val hints: List<String> = emptyList(),
    val extensions: Map<String, String> = emptyMap()
) {
    companion object {
        const val TIME_OUT_VALUE = 1800000L
    }
}
