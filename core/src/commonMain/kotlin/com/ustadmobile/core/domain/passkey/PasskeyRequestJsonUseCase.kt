package com.ustadmobile.core.domain.passkey

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.ustadmobile.core.MR
import io.ktor.util.encodeBase64
import kotlin.random.Random

class PasskeyRequestJsonUseCase(
    private val systemImpl: UstadMobileSystemImpl,
    private val json: Json,
    ) {

    /**
     * https://developer.android.com/identity/sign-in/credential-manager#format-json-request
     * creating request json for createCredential request ,
     * here we are taking   username,  personUid,doorNodeIdu,sStartTime  to create challenge
     * challenge is  encoded string , created with above params
     *
     *serverurl is added in id because it will be used during signin with passkey, suppose there
     * are multiple domain , and we dont know from which domain user created the passkey ,
     * so adding @serverUrl in id we can check during signin which domain user registered
     */
    fun createPasskeyRequestJson(
        createPasskeyParams: CreatePasskeyParams
    ): String {
        val userId = randomString(16)
        val challenge = json.encodeToString(
            UserPasskeyChallenge(
                username = createPasskeyParams.username,
                personUid = createPasskeyParams.personUid,
                doorNodeId = createPasskeyParams.doorNodeId,
                usStartTime = createPasskeyParams.usStartTime
            )
        )

        val challengeBase64Encoded =   challenge.encodeBase64()
        val useridBase64Encoded =  "$userId@${createPasskeyParams.serverUrl}".encodeBase64()

        val requestJson = """
                  {
                    "challenge": "${challengeBase64Encoded}",
                    "rp": {
                      "id": "credential-manager-${createPasskeyParams.domainName}",
                      "name": "${systemImpl.getString(MR.strings.app_name)}"
                    },
                    "pubKeyCredParams": [
                      {
                        "type": "public-key",
                        "alg": -7
                      },
                      {
                        "type": "public-key",
                        "alg": -257
                      }
                    ],
                    "authenticatorSelection": {
                      "authenticatorAttachment": "platform",
                      "residentKey": "required"
                    },
                    "user": {
                      "id": "$useridBase64Encoded",
                      "name": "${createPasskeyParams.username}",
                      "displayName": "${createPasskeyParams.username}"
                    }
                  }
              """.trimIndent()

        Napier.e { requestJson }
        return requestJson
    }


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