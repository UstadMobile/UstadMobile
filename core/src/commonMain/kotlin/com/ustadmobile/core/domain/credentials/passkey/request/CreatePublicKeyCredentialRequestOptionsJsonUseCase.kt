package com.ustadmobile.core.domain.credentials.passkey.request

import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialRequestOptionsJSON
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialDescriptorJSON
import com.ustadmobile.lib.util.randomString
import io.ktor.http.Url
import io.ktor.util.encodeBase64

/**
 * Create the Json that is used to request an (existing) passkey for sign-in.
 *
 * This matches the PublicKeyCredentialRequestOptionsJSON spec as per:
 * https://w3c.github.io/webauthn/#dictdef-publickeycredentialrequestoptionsjson
 *
 * See CreatePublicKeyCredentialCreationOptionsJsonUseCase for further details on how passkeys are
 * handled.
 */
class CreatePublicKeyCredentialRequestOptionsJsonUseCase(
    private val systemUrlConfig: SystemUrlConfig,
) {

    operator fun invoke(
        allowCredentials: List<PublicKeyCredentialDescriptorJSON> = emptyList()
    ): PublicKeyCredentialRequestOptionsJSON {
        val challenge = randomString(16)

        return PublicKeyCredentialRequestOptionsJSON(
            challenge = challenge.encodeBase64(),
            timeout = PublicKeyCredentialRequestOptionsJSON.TIME_OUT_VALUE,
            rpId = Url(systemUrlConfig.systemBaseUrl).host,
            allowCredentials = allowCredentials,
            userVerification = "required"
        )
    }

}
