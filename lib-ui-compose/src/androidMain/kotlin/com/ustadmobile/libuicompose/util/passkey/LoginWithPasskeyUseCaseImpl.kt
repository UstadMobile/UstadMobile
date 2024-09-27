package com.ustadmobile.libuicompose.util.passkey

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.GetCredentialException
import com.google.common.collect.DiscreteDomain
import com.ustadmobile.core.domain.passkey.LoginWithPasskeyUseCase
import com.ustadmobile.core.domain.passkey.PassKeySignInData
import com.ustadmobile.core.domain.passkey.PasskeyRequestJsonUseCase
import io.github.aakira.napier.Napier
import org.json.JSONObject

class LoginWithPasskeyUseCaseImpl(
    val context: Context,
    val passkeyRequestJsonUseCase: PasskeyRequestJsonUseCase

) : LoginWithPasskeyUseCase {
    override suspend fun invoke(domain: String): PassKeySignInData? {
        val credentialManager = CredentialManager.create(context)

        var passKeySignInData: PassKeySignInData? = null
        val getPasswordOption = GetPasswordOption()

        // Get passkey from the user's public key credential provider.
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = passkeyRequestJsonUseCase.requestJsonForSignIn(domain)
        )
        val getCredRequest = GetCredentialRequest(
            listOf(getPasswordOption, getPublicKeyCredentialOption)
        )

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = getCredRequest
            )
            Napier.d { "GetCredentialResponse data: ${result.credential.data}" }
            // Extract the JSON string using the key from the bundle
            val authResponseJson = result.credential.data.getString(
                "androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON"
            )

            // Parse the JSON string if it's not null
            if (authResponseJson != null) {
                val jsonObject = JSONObject(authResponseJson)
                val responseObject = jsonObject.getJSONObject("response")

                val clientDataJsonString = responseObject.getString("clientDataJSON")
                val decodedBytes = Base64.decode(clientDataJsonString, Base64.DEFAULT)
                val decodedJson = String(decodedBytes)
                val clientDataJson = JSONObject(decodedJson)

                // Create the PassKeySignInData object.
                passKeySignInData = PassKeySignInData(
                    credentialId = jsonObject.getString("id"),
                    userHandle = responseObject.getString("userHandle"),
                    authenticatorData = responseObject.getString("authenticatorData"),
                    clientDataJSON = clientDataJsonString,
                    signature = responseObject.getString("signature"),
                    origin = clientDataJson.getString("origin"),
                    rpId = "credential-manager-subdomain.applinktest.ustadmobile.com",  // Replace with the actual rpId if needed.
                    challenge = clientDataJson.getString("challenge"),
                )


            } else {
                Napier.e { "Auth response JSON is null." }
            }

        } catch (e: GetCredentialException) {
            Log.e("getCredential", "getCredential failed with exception message: ${e.message}")
            Napier.e(e) { "GetCredentialException: ${e.message}" }
        }
        return passKeySignInData

    }
}