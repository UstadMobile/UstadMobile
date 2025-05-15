package com.ustadmobile.core.domain.credentials.passkey

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import com.ustadmobile.core.domain.credentials.CreatePasskeyRequestJsonUseCase
import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON
import com.ustadmobile.core.impl.config.SystemUrlConfig
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class GetCredentialUseCaseImpl(
    private val context: Context,
    private val passkeyRequestJsonUseCase: CreatePasskeyRequestJsonUseCase,
    private val apiUrlConfig: SystemUrlConfig,
    private val json: Json,
) : GetCredentialUseCase {

    override suspend fun invoke(): GetCredentialUseCase.CredentialResult {
        val credentialManager = CredentialManager.create(context)

        val getPasswordOption = GetPasswordOption()
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = passkeyRequestJsonUseCase.requestJsonForSignIn()
        )

        //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in when
        // preferImmediatelyAvailableCredentials = true then the dialog will only be shown if the
        // user has accounts that they can select.
        val getCredentialRequest = GetCredentialRequest(
            credentialOptions = listOf(getPasswordOption, getPublicKeyCredentialOption),
            preferImmediatelyAvailableCredentials = true
        )

        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = getCredentialRequest
            )

            //As per https://developer.android.com/identity/sign-in/credential-manager#sign-in
            when (val credential = result.credential) {
                is PasswordCredential -> {
                    GetCredentialUseCase.PasswordCredentialResult(
                        credentialUsername = credential.id,
                        password = credential.password
                    )
                }

                is PublicKeyCredential -> {
                    val authResponseJson = credential.authenticationResponseJson
                    Napier.d {"passkey response ${authResponseJson}"}

                    if (authResponseJson != null) {
                        val parsedResponse = json.decodeFromString<AuthenticationResponseJSON>(authResponseJson)


                        GetCredentialUseCase.PasskeyCredentialResult(
                           parsedResponse
                        )
                    } else {
                        GetCredentialUseCase.Error("Auth response JSON is null.")
                    }
                }

                else -> {
                    GetCredentialUseCase.Error("Unknown credential type.")
                }
            }
        } catch (e: NoCredentialException) {
            GetCredentialUseCase.Error("No credentials found: ${e.message}")
        } catch (e: GetCredentialException) {
            GetCredentialUseCase.Error("Failed to get credential: ${e.message}")
        }
    }
}