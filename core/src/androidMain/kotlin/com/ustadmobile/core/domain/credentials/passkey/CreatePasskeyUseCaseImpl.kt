package com.ustadmobile.core.domain.credentials.passkey

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase
import com.ustadmobile.core.domain.credentials.CreatePasskeyUseCase.CreatePasskeyResult
import io.github.aakira.napier.Napier
import com.ustadmobile.core.domain.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Create a passkey on Android. This will show a bottom sheet for the user to aprove creating a new
 * passkey.
 *
 * @param context : this must be an activity context as per the Android docs
 *
 * See https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
class CreatePasskeyUseCaseImpl(
    val context: Context,
    private val json: Json,
    private val createPublicKeyJsonUseCase: CreatePublicKeyCredentialCreationOptionsJsonUseCase
) : CreatePasskeyUseCase {

    /**
     * @throws CreateCredentialException if CredentialManager throws an exception
     */
    @SuppressLint("PublicKeyCredential")
    override suspend fun invoke(username: String): CreatePasskeyResult {
        val credentialManager = CredentialManager.create(context)

        return try {
            val request = CreatePublicKeyCredentialRequest(
                requestJson = json.encodeToString(
                    createPublicKeyJsonUseCase(username)
                ),
                preferImmediatelyAvailableCredentials = false,
            )
            val response = credentialManager.createCredential(
                context,
                request
            ) as CreatePublicKeyCredentialResponse

            Napier.d { "passkey response: ${response.registrationResponseJson}" }
            val passkeyResponse =
                json.decodeFromString<AuthenticationResponseJSON>(response.registrationResponseJson)

            CreatePasskeyUseCase.PasskeyCreatedResult(passkeyResponse)
        } catch (e: CreateCredentialCancellationException) {
            CreatePasskeyUseCase.UserCanceledResult()
        } catch (e: CreateCredentialException) {
            // See https://codelabs.developers.google.com/credential-manager-api-for-android#1
            Napier.e(
                message = "CreatePassKeyUseCaseImpl: exception", throwable = e
            )
            CreatePasskeyUseCase.Error(e.message)
        }
    }
}