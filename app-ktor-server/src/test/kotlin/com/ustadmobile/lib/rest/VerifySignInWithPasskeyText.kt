package com.ustadmobile.lib.rest

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
import com.webauthn4j.util.Base64UrlUtil
import org.junit.Before
import org.junit.Test
import java.util.Base64

class VerifySignInWithPasskeyTest {

    private lateinit var webAuthnManager: WebAuthnManager

    @Before
    fun setup() {
        webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager()
    }

    @Test
    fun verifyPasskeySignInWithValidData() {
        val credentialId = "7mh8FT3lmxDDYhAYbycguA"
        val credentialIdByte = Base64.getUrlDecoder().decode(credentialId)
        val userHandleByte = Base64.getUrlDecoder().decode("b99mivwbh2zi7j2y")
        val authenticatorDataByte = Base64.getUrlDecoder().decode("-tR2qGS5dbnHy8r5VUW8iieLOjcn4ro4oSPJAgYup4wdAAAAAA")
        val clientDataJSONByte = Base64.getUrlDecoder().decode("eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoianRkZXgzcmY3ZGMyenJoeiIsIm9yaWdpbiI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkZoT2gyZjhXc20xaE1hQkNTVjdDVFNZVWRkVlFMbXpmODJyZVRmSnI0T1kiLCJhbmRyb2lkUGFja2FnZU5hbWUiOiJjb20udG91Z2hyYS51c3RhZG1vYmlsZSJ9")
        val signatureByte = Base64.getUrlDecoder().decode("MEUCIEOjWL_KknszZpVuvqr8KWvvuGKDnSOjsHeXA8d_HGaCAiEAqKteyVaaCRSKpWd2ZmGYXqeYnQSW2G8dmzNW0YMJn7M")
        val origin = "android:apk-key-hash:FhOh2f8Wsm1hMaBCSV7CTSYUddVQLmzf82reTfJr4OY"
        val rpId = "credential-manager-subdomain.applinktest.ustadmobile.com"
        val challenge = "jtdex3rf7dc2zrhz"

        // Server properties
        val serverOrigin = Origin(origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Load credential record from storage
        val credentialRecord = loadCredentialRecord()

        // Prepare the authentication request
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
                null,
                true,
                true
            )
        }

        try {
            val authenticationData = webAuthnManager.parse(authenticationRequest)
            if (authenticationParameters != null) {
                val result = webAuthnManager.verify(authenticationData, authenticationParameters)
                println("Verification result: $result")
            } else {
                throw IllegalArgumentException("Authentication parameters are null")
            }
        } catch (e: DataConversionException) {
            println("Data conversion error: ${e.message}")
        } catch (e: Exception) {
            println("Verification error: ${e.message}")
        }
    }
    @Test
    fun verifyPasskeySignInWithWrongChallenge() {
        val credentialId = "7mh8FT3lmxDDYhAYbycguA"
        val credentialIdByte = Base64.getUrlDecoder().decode(credentialId)
        val userHandleByte = Base64.getUrlDecoder().decode("b99mivwbh2zi7j2y")
        val authenticatorDataByte = Base64.getUrlDecoder().decode("-tR2qGS5dbnHy8r5VUW8iieLOjcn4ro4oSPJAgYup4wdAAAAAA")
        val clientDataJSONByte = Base64.getUrlDecoder().decode("eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoianRkZXgzcmY3ZGMyenJoeiIsIm9yaWdpbiI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkZoT2gyZjhXc20xaE1hQkNTVjdDVFNZVWRkVlFMbXpmODJyZVRmSnI0T1kiLCJhbmRyb2lkUGFja2FnZU5hbWUiOiJjb20udG91Z2hyYS51c3RhZG1vYmlsZSJ9")
        val signatureByte = Base64.getUrlDecoder().decode("MEUCIEOjWL_KknszZpVuvqr8KWvvuGKDnSOjsHeXA8d_HGaCAiEAqKteyVaaCRSKpWd2ZmGYXqeYnQSW2G8dmzNW0YMJn7M")
        val origin = "android:apk-key-hash:FhOh2f8Wsm1hMaBCSV7CTSYUddVQLmzf82reTfJr4OY"
        val rpId = "credential-manager-subdomain.applinktest.ustadmobile.com"
        val challenge = "tdex3rf7dc2zrhz"

        // Server properties
        val serverOrigin = Origin(origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Load credential record from storage
        val credentialRecord = loadCredentialRecord()

        // Prepare the authentication request
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
                null,
                true,
                true
            )
        }

        try {
            val authenticationData = webAuthnManager.parse(authenticationRequest)
            if (authenticationParameters != null) {
                val result = webAuthnManager.verify(authenticationData, authenticationParameters)
                println("Verification result: $result")
            } else {
                throw IllegalArgumentException("Authentication parameters are null")
            }
        } catch (e: DataConversionException) {
            println("Data conversion error: ${e.message}")
        } catch (e: Exception) {
            println("Verification error: ${e.message}")
        }
    }

    @Test
    fun verifyPasskeySignInWithWrongSignature() {
        val credentialId = "7mh8FT3lmxDDYhAYbycguA"
        val credentialIdByte = Base64.getUrlDecoder().decode(credentialId)
        val userHandleByte = Base64.getUrlDecoder().decode("b99mivwbh2zi7j2y")
        val authenticatorDataByte = Base64.getUrlDecoder().decode("-tR2qGS5dbnHy8r5VUW8iieLOjcn4ro4oSPJAgYup4wdAAAAAA")
        val clientDataJSONByte = Base64.getUrlDecoder().decode("eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoianRkZXgzcmY3ZGMyenJoeiIsIm9yaWdpbiI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkZoT2gyZjhXc20xaE1hQkNTVjdDVFNZVWRkVlFMbXpmODJyZVRmSnI0T1kiLCJhbmRyb2lkUGFja2FnZU5hbWUiOiJjb20udG91Z2hyYS51c3RhZG1vYmlsZSJ9")
        val signatureByte = Base64.getUrlDecoder().decode("EUCIEOjWL_KknszZpVuvqr8KWvvuGKDnSOjsHeXA8d_HGaCAiEAqKteyVaaCRSKpWd2ZmGYXqeYnQSW2G8dmzNW0YMJn7M")
        val origin = "android:apk-key-hash:FhOh2f8Wsm1hMaBCSV7CTSYUddVQLmzf82reTfJr4OY"
        val rpId = "credential-manager-subdomain.applinktest.ustadmobile.com"
        val challenge = "jtdex3rf7dc2zrhz"

        // Server properties
        val serverOrigin = Origin(origin)
        val serverChallenge = DefaultChallenge(Base64.getUrlDecoder().decode(challenge))
        val tokenBindingId: ByteArray? = null
        val serverProperty = ServerProperty(serverOrigin, rpId, serverChallenge, tokenBindingId)

        // Load credential record from storage
        val credentialRecord = loadCredentialRecord()

        // Prepare the authentication request
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
                null,
                true,
                true
            )
        }

        try {
            val authenticationData = webAuthnManager.parse(authenticationRequest)
            if (authenticationParameters != null) {
                val result = webAuthnManager.verify(authenticationData, authenticationParameters)
                println("Verification result: $result")
            } else {
                throw IllegalArgumentException("Authentication parameters are null")
            }
        } catch (e: DataConversionException) {
            println("Data conversion error: ${e.message}")
        } catch (e: Exception) {
            println("Verification error: ${e.message}")
        }
    }

    private fun loadCredentialRecord(): CredentialRecord? {
        val attestationObject = Base64.getUrlDecoder().decode("o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViU-tR2qGS5dbnHy8r5VUW8iieLOjcn4ro4oSPJAgYup4xdAAAAAOqbjWZNAR0hPOS2tIy1ddQAEO5ofBU95ZsQw2IQGG8nILilAQIDJiABIVggzDRVQ135poNxBFcJaGl1Z9M-elBDgeTK2mBcjUyX0_kiWCAQlP_Rat6O_a6gCjvURK5gJ94A_PVMhZNNR8vb5kgNxw")
        val clientDataJSONByte = Base64.getUrlDecoder().decode("eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoianRkZXgzcmY3ZGMyenJoeiIsIm9yaWdpbiI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkZoT2gyZjhXc20xaE1hQkNTVjdDVFNZVWRkVlFMbXpmODJyZVRmSnI0T1kiLCJhbmRyb2lkUGFja2FnZU5hbWUiOiJjb20udG91Z2hyYS51c3RhZG1vYmlsZSJ9")
        val clientExtensionJSON: String? = null
        val transports: Set<String> = setOf("internal", "hybrid")

        val registrationRequest = RegistrationRequest(
            attestationObject,
            clientDataJSONByte,
            clientExtensionJSON,
            transports
        )

        val registrationData: RegistrationData
        return try {
            registrationData = webAuthnManager.parse(registrationRequest)
            registrationData.attestationObject?.let {
                CredentialRecordImpl(
                    it,
                    registrationData.collectedClientData,
                    registrationData.clientExtensions,
                    registrationData.transports
                )
            }
        } catch (e: DataConversionException) {
            println("Data conversion error in loadCredentialRecord: ${e.message}")
            null
        }
    }
}



