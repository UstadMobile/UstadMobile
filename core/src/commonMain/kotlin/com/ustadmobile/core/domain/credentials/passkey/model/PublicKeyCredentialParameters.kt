package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

//As per https://w3c.github.io/webauthn/#dictdef-publickeycredentialparameters
@Serializable
data class PublicKeyCredentialParameters(
    val type: String,
    val alg: Int,
) {
    companion object {

        const val TYPE_PUBLIC_KEY = "public-key"

        const val ALGORITHM_ES256 = -7

        const val ALGORITHM_RS256 = -257

    }
}