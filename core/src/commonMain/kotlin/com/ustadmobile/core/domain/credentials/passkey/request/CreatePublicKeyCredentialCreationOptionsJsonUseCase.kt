package com.ustadmobile.core.domain.credentials.passkey.request

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialCreationOptionsJSON
import com.ustadmobile.core.domain.credentials.passkey.model.PublicKeyCredentialRpEntity
import com.ustadmobile.lib.util.randomString
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
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
 * The passkey user.id will be a unique 64bit long @
 *
 * The passkey userHandle (e.g. PublicKeyCredentialUserEntityJSON.id) will be
 * "passkeyUid@https://learningspace.example.org/" where passkeyUid is the UID of the PersonPasskey
 * entity in the database and https://learningspace.example.org/ is the learning space url.
 * As per https://w3c.github.io/webauthn/#dictionary-user-credential-params the userHandle MUST
 * NOT contain any personally identifiable information (like usernames, email, phone etc).
 *
 * This user handle allows the app to verify the passkey on the server because it includes a) the
 * learning space url and b) the uid of the passkey itself. The server can then authenticate a
 * credential manager response (including determining the related user).
 */
class CreatePublicKeyCredentialCreationOptionsJsonUseCase(
    private val systemUrlConfig: SystemUrlConfig,
    private val systemImpl: UstadMobileSystemImpl,
    private val createCredentialUsernameUseCase: CreateCredentialUsernameUseCase,
    private val learningSpace: LearningSpace,
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