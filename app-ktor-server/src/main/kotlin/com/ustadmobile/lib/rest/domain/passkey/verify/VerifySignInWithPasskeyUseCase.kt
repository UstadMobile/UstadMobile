package com.ustadmobile.lib.rest.domain.passkey.verify

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.credentials.PasskeyVerifyResult
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON
import com.ustadmobile.core.domain.credentials.passkey.model.ClientDataJSON
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.PersonPasskey
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.credential.CredentialRecord
import com.webauthn4j.credential.CredentialRecordImpl
import com.webauthn4j.data.AuthenticationData
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.AuthenticationRequest
import com.webauthn4j.data.RegistrationData
import com.webauthn4j.data.RegistrationRequest
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import kotlinx.serialization.json.Json
import java.util.Base64

class VerifySignInWithPasskeyUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val json: Json,
) {

    private val webAuthnManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

    var result: AuthenticationData? = null

    /**
     * TODO: Document what this code is based on and provide link
     */
    suspend operator fun invoke(
        authenticationResponseJSON: AuthenticationResponseJSON,
        rpId: String,
    ): PasskeyVerifyResult {

        val effectiveDb = repo ?: db

        val clientDataJSONBase64 = authenticationResponseJSON.response.clientDataJSON
        val decodedBytes = Base64.getDecoder().decode(clientDataJSONBase64)
        val clientDataJson = json.decodeFromString<ClientDataJSON>(decodedBytes.decodeToString())
        // Client properties
        val credentialIdByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.id)
        val userHandleByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.userHandle)
        val authenticatorDataByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.authenticatorData)
        val clientDataJSONByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.clientDataJSON)
        val signatureByte = Base64.getUrlDecoder().decode(authenticationResponseJSON.response.signature)

        // Server properties
        val serverOrigin = Origin(clientDataJson.origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(clientDataJson.challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Expectations
        val allowCredentials: List<ByteArray>? = null
        val userVerificationRequired = true
        val userPresenceRequired = true

        val passkeyData = effectiveDb.withDoorTransactionAsync {
            effectiveDb.personPasskeyDao().findPersonPasskeyFromClientDataJson(authenticationResponseJSON.id)
        }
        val credentialRecord = createCredentialRecord( passkeyData)

        val authenticationRequest = AuthenticationRequest(
            credentialIdByte,
            userHandleByte,
            authenticatorDataByte,
            clientDataJSONByte,
            null,
            signatureByte
        )
        val authenticationParameters = credentialRecord?.let {
            AuthenticationParameters(
                serverProperty,
                it,
                allowCredentials,
                userVerificationRequired,
                userPresenceRequired
            )
        }

        val authenticationData: AuthenticationData
        try {
            authenticationData = webAuthnManager.parse(authenticationRequest)
        } catch (e: DataConversionException) {
            throw e
        }

        try {
            if (authenticationParameters != null) {
                result = webAuthnManager.verify(authenticationData, authenticationParameters)
            }
        } catch (e: Exception) {
            throw e
        }
        return if (result != null) {
            PasskeyVerifyResult(isVerified = true,passkeyData?.ppPersonUid?:0L)
        } else {
            PasskeyVerifyResult(isVerified = false,0L)
        }
    }

    private fun createCredentialRecord(
        passkeyData: PersonPasskey?,
    ): CredentialRecord?{

        var credentialRecord: CredentialRecord? = null

        passkeyData?.let {
            // Client properties
            val attestationObject = Base64.getUrlDecoder().decode(it.ppAttestationObj)
            val clientDataJSON = Base64.getUrlDecoder().decode(it.ppClientDataJson)
            val clientExtensionJSON: String? = null
            val transports: Set<String> = setOf("internal", "hybrid")

            val registrationRequest = RegistrationRequest(
                attestationObject,
                clientDataJSON,
                clientExtensionJSON,
                transports
            )

            val registrationData: RegistrationData
            try {
                registrationData = webAuthnManager.parse(registrationRequest)
            } catch (e: DataConversionException) {
                throw e
            }

            // Persist CredentialRecord object, which will be used in the authentication process.
            credentialRecord = registrationData.attestationObject?.let { it1 ->
                CredentialRecordImpl(
                    it1,
                    registrationData.collectedClientData,
                    registrationData.clientExtensions,
                    registrationData.transports
                )
            }
        }
        return credentialRecord
    }
}
