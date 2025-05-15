package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.config.SystemUrlConfig
import io.ktor.http.Url
import io.ktor.util.encodeBase64
import kotlin.random.Random

/**
 * As per https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 * the passkey creation request Json must follow the schema as per
 * https://w3c.github.io/webauthn/#dictdef-publickeycredentialcreationoptionsjson
 *
 * https://developer.android.com/identity/sign-in/credential-manager#format-json-request
 * creating request json for createCredential request ,
 * here we are taking   username,  personUid,doorNodeIdu,sStartTime  to create challenge
 * challenge is  encoded string , created with above params
 *
 * serverurl is added in id because it will be used during signin with passkey, suppose there
 * are multiple domain , and we dont know from which domain user created the passkey ,
 * so adding @serverUrl in id we can check during signin which domain user registered
 */
@Deprecated("Replaced by CreatePublicKeyCredentialCreationOptionsJsonUseCase")
class CreatePasskeyRequestJsonUseCase(
    private val systemImpl: UstadMobileSystemImpl,
    private val systemUrlConfig: SystemUrlConfig,
    private val json: Json,
) {

    fun randomString(length: Int): String {
        val charPool = "abcdefghikjmnpqrstuvxwyz23456789"

        return (1..length).map { i -> charPool.get(Random.nextInt(0, charPool.length)) }
            .joinToString(separator = "")
    }

    fun requestJsonForSignIn(domain: String): String {
        val challenge = randomString(16)

        val requestJson = """
    {
    "challenge": "$challenge",
    "allowCredentials": [],
    "timeout": 1800000,
    "userVerification": "required",
    "rpId": "credential-manager-${domain}"
}
""".trimIndent()
        Napier.e { requestJson }
        return requestJson
    }
}