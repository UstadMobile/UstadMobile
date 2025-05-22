package com.ustadmobile.core.domain.credentials.passkey.model

import kotlinx.serialization.Serializable

//As per https://w3c.github.io/webauthn/#dictdef-publickeycredentialcreationoptionsjson
//See also https://medium.com/androiddevelopers/bringing-seamless-authentication-to-your-apps-using-credential-manager-api-b3f0d09e0093#172a
@Serializable
class PublicKeyCredentialCreationOptionsJSON(
    val rp: PublicKeyCredentialRpEntity,
    val user: PublicKeyCredentialUserEntityJSON,
    val challenge: String,
    val pubKeyCredParams: List<PublicKeyCredentialParameters>,
    val timeout: Long? = null,
    val excludeCredentials: List<PublicKeyCredentialDescriptorJSON> = emptyList(),
    val authenticatorSelection: AuthenticatorSelectionCriteria? = null,
    val hints: List<String> = emptyList(),
    val attestation: String = "none",
    val attestationFormats: List<String> = emptyList(),
    val extensions: Map<String, String> = emptyMap()
) {
    companion object {
        //the time out value is required for for showing google password manager
        const val TIME_OUT_VALUE : Long = 1800000

    }
}


