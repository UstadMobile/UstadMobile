package com.ustadmobile.core.domain.credentials.passkey.request

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialCreationOptionsJSON
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialRpEntity
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticatorSelectionCriteria
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialParameters
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialUserEntityJSON
import com.ustadmobile.core.domain.credentials.username.CreateCredentialUsernameUseCase
import io.ktor.http.Url
import io.ktor.util.encodeBase64

class CreatePasskeyRequestJsonServerUseCase(
    private val systemUrlConfig: SystemUrlConfig,
    private val systemImpl: UstadMobileSystemImpl,
    private val createCredentialUsernameUseCase: CreateCredentialUsernameUseCase,
) {

    operator fun invoke(
        username: String,
    ): PublicKeyCredentialCreationOptionsJSON {
        val challenge = randomString(16)
        val credentialUsername = createCredentialUsernameUseCase(
            username = username,
        )

        return PublicKeyCredentialCreationOptionsJSON(
            rp = PublicKeyCredentialRpEntity(
                id = Url(systemUrlConfig.systemBaseUrl).host,
                name = systemImpl.getString(MR.strings.app_name),
                icon = null,
            ),
            user = PublicKeyCredentialUserEntityJSON(
                id = uuid4().toString(),
                name = credentialUsername,
                displayName = credentialUsername,
            ),
            challenge = challenge.encodeBase64(),
            pubKeyCredParams = listOf(
                PublicKeyCredentialParameters(
                    type = PublicKeyCredentialParameters.TYPE_PUBLIC_KEY,
                    alg = PublicKeyCredentialParameters.ALGORITHM_ES256
                ),
                PublicKeyCredentialParameters(
                    type = PublicKeyCredentialParameters.TYPE_PUBLIC_KEY,
                    alg = PublicKeyCredentialParameters.ALGORITHM_RS256
                ),
            ),
            authenticatorSelection = AuthenticatorSelectionCriteria(
                authenticatorAttachment = "platform",
                residentKey = "required"
            )
        )
    }

}