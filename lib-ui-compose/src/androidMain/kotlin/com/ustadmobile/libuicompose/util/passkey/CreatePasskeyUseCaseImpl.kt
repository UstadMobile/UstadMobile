package com.ustadmobile.libuicompose.util.passkey

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.CreatePasskeyUseCase
import com.ustadmobile.core.domain.passkey.PasskeyResult
import io.github.aakira.napier.Napier
import org.json.JSONObject


/**
 * the CreatePasskeyPrompt will show the google bottomsheet to create passkey
 * https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
class CreatePasskeyUseCaseImpl(val context: Context) : CreatePasskeyUseCase {
    @SuppressLint("PublicKeyCredential")
    override suspend fun invoke(createPassKeyParams: CreatePasskeyParams): PasskeyResult? {
        val credentialManager = CredentialManager.create(context)

        /**credentialManager to create credential requires a request
         * https://developer.android.com/identity/sign-in/credential-manager#format-json-request
         */
        val request = CreatePublicKeyCredentialRequest(
            PasskeyRequestJsonUseCase.createPasskeyRequestJson(
               createPassKeyParams
            )
        )
        var passkeyResult: PasskeyResult? = null
        try {
            val response = credentialManager.createCredential(
                context,
                request
            ) as CreatePublicKeyCredentialResponse

            Napier.d { "passkey response: ${response.registrationResponseJson}" }

            val jsonObject = JSONObject(response.registrationResponseJson)
            val responseObject = jsonObject.getJSONObject("response")

            val clientDataJsonString = responseObject.getString("clientDataJSON")
            val attestationObject = responseObject.getString("attestationObject")
            val publicKey = responseObject.getString("publicKey")
            val id = jsonObject.getString("id")

            val decodedClientDataJsonBytes = Base64.decode(clientDataJsonString, Base64.DEFAULT)
            val decodedClientDataJson = String(decodedClientDataJsonBytes)
            val clientDataJsonObject = JSONObject(decodedClientDataJson)

            val originString = clientDataJsonObject.optString("origin", "")
            val challengeString = clientDataJsonObject.optString("challenge", "")

            passkeyResult = PasskeyResult(
                attestationObj = attestationObject,
                clientDataJson = clientDataJsonString,
                originString = originString,
                rpid = "credential-manager-subdomain.applinktest.ustadmobile.com",
                challengeString = challengeString,
                publicKey = publicKey,
                id = id,
                personUid = createPassKeyParams.personUid.toLong(),
                person=createPassKeyParams.person
            )
        } catch (e: CreateCredentialException) {
            Napier.e(e) { "Failed to create passkey: ${e.message}" }
        }

        return passkeyResult
    }
}