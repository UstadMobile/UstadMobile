package com.ustadmobile.libuicompose.util.passkey

import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.UserPasskeyChallenge
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64
import kotlin.random.Random

object PasskeyRequestJsonUseCase {

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
        val challenge = Json.encodeToString(
            UserPasskeyChallenge(
                username = createPasskeyParams.username,
                personUid = createPasskeyParams.personUid,
                doorNodeId = createPasskeyParams.doorNodeId,
                usStartTime = createPasskeyParams.usStartTime
            )
        )

        val challengeBase64Encoded = Base64.getEncoder().encodeToString(challenge.toByteArray())
        val useridBase64Encoded = Base64.getEncoder().encodeToString("$userId@${createPasskeyParams.serverUrl}".toByteArray())

        val requestJson = """
                  {
                    "challenge": "${challengeBase64Encoded}",
                    "rp": {
                      "id": "credential-manager-${createPasskeyParams.domainName}",
                      "name": "Ustad Mobile"
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