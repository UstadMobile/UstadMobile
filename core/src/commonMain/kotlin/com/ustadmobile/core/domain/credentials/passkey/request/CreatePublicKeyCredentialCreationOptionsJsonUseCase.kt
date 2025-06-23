package com.ustadmobile.core.domain.credentials.passkey.request

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialCreationOptionsJSON
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialRpEntity
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.credentials.passkey.EncodeUserHandleUseCase
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticatorSelectionCriteria
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialParameters
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialUserEntityJSON
import com.ustadmobile.core.domain.credentials.username.CreateCredentialUsernameUseCase
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.PersonPasskey
import io.ktor.http.Url
import io.ktor.util.encodeBase64

/**
 * Create the Json that is used to request creation of a new passkey. This should work on all
 * platforms where passkeys are supported (Android, Web, etc).
 *
 * As per https://w3c.github.io/webauthn/#dictdef-publickeycredentialcreationoptionsjson
 *
 * The passkey rpId will always be the SystemUrlConfig.systemBaseUrl host
 * The passkey user.id will be a unique 64bit long.
 *
 * The userHandle (PublicKeyCredentialUserEntityJSON.id) is encoded to include the person passkey
 * UID and Learning Space URL - see EncodeUserHandleUseCase
 */
class CreatePublicKeyCredentialCreationOptionsJsonUseCase(
    private val systemUrlConfig: SystemUrlConfig,
    private val systemImpl: UstadMobileSystemImpl,
    private val createCredentialUsernameUseCase: CreateCredentialUsernameUseCase,
    private val db: UmAppDatabase,
    private val encodeUserHandleUseCase : EncodeUserHandleUseCase,
) {

    operator fun invoke(
        username: String,
    ): PublicKeyCredentialCreationOptionsJSON {
        val challenge = randomString(16) //TODO note: this should really take place on the server side
        val credentialUsername = createCredentialUsernameUseCase(
            username = username,
        )

        val personPasskeyUid = db.doorPrimaryKeyManager.nextId(PersonPasskey.TABLE_ID)
        val encodeUserHandle = encodeUserHandleUseCase(personPasskeyUid)

        return PublicKeyCredentialCreationOptionsJSON(
            rp = PublicKeyCredentialRpEntity(
                id = Url(systemUrlConfig.systemBaseUrl).host,
                name = systemImpl.getString(MR.strings.app_name),
                icon = null,
            ),
            user = PublicKeyCredentialUserEntityJSON(
                id = encodeUserHandle,
                name = credentialUsername,
                displayName = credentialUsername,
            ),
            //Important: timeout may be optional as per the spec, but if omitted, Google Password
            //Manager won't work as expected
            timeout = PublicKeyCredentialCreationOptionsJSON.TIME_OUT_VALUE,
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