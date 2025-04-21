package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

//See https://w3c.github.io/webauthn/#dictdef-publickeycredentialdescriptorjson
@Serializable
class PublicKeyCredentialDescriptorJSON(
    val type: String,
    val id: String,
    val transports: List<String>,
)
