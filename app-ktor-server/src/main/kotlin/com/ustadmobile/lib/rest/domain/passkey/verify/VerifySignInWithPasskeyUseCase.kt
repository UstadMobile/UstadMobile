package com.ustadmobile.lib.rest.domain.passkey.verify

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.passkey.PasskeyVerifyResult
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
import java.util.Base64

class VerifySignInWithPasskeyUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    private val webAuthnManager: WebAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()
    var result: AuthenticationData? = null
    suspend operator fun invoke(
        credentialId: String,
        userHandle: String,
        authenticatorData: String,
        clientDataJSON: String,
        signature: String,
        origin: String,
        rpId: String,
        challenge: String,
    ): PasskeyVerifyResult {

        val effectiveDb = repo ?: db

        // Client properties
        val credentialIdByte = Base64.getUrlDecoder().decode(credentialId)
        val userHandleByte = Base64.getUrlDecoder().decode(userHandle)
        val authenticatorDataByte = Base64.getUrlDecoder().decode(authenticatorData)
        val clientDataJSONByte = Base64.getUrlDecoder().decode(clientDataJSON)
        val signatureByte = Base64.getUrlDecoder().decode(signature)

        // Server properties
        val serverOrigin = Origin(origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Expectations
        val allowCredentials: List<ByteArray>? = null
        val userVerificationRequired = true
        val userPresenceRequired = true

        val passkeyData = effectiveDb.withDoorTransactionAsync {
            effectiveDb.personPasskeyDao().findPersonPasskeyFromClientDataJson(credentialId)
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

    private  fun createCredentialRecord(
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
