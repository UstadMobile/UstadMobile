package com.ustadmobile.core.domain.credentials.passkey

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import com.ustadmobile.core.domain.credentials.CreatePasskeyParams
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase
import io.github.aakira.napier.Napier
import org.json.JSONObject
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase.CreatePasskeyResult
import com.ustadmobile.core.domain.credentials.CreatePasskeyRequestJsonUseCase

/**
 * the CreatePasskeyPrompt will show the google bottomsheet to create passkey
 * https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
class CreatePasskeyUseCaseImpl(
    val context: Context,
    val createPasskeyRequestJsonUseCase: CreatePasskeyRequestJsonUseCase
) : CreatePasskeyUseCase {

    /**
     * @throws CreateCredentialException if CredentialManager throws an exception
     */
    @SuppressLint("PublicKeyCredential")
    override suspend fun invoke(createPassKeyParams: CreatePasskeyParams): CreatePasskeyResult {
        val credentialManager = CredentialManager.create(context)

        /** credentialManager to create credential requires a request
         * https://developer.android.com/identity/sign-in/credential-manager#format-json-request
         */
        try {
            val request = CreatePublicKeyCredentialRequest(
                requestJson = createPasskeyRequestJsonUseCase(createPassKeyParams),
                preferImmediatelyAvailableCredentials = false,
            )
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

            return CreatePasskeyResult(
                attestationObj = attestationObject,
                clientDataJson = clientDataJsonString,
                originString = originString,
                rpid = "credential-manager-${createPassKeyParams.masterDomainName}",
                challengeString = challengeString,
                publicKey = publicKey,
                id = id,
                personUid = createPassKeyParams.personUid.toLong(),
                person=createPassKeyParams.person
            )
        } catch (e: CreateCredentialException) {
            // See https://codelabs.developers.google.com/credential-manager-api-for-android#1
            Napier.e(
                message = "CreatePassKeyUseCaseImpl: exception", throwable = e
            )
            throw e
        }
    }
}