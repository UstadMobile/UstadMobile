package com.ustadmobile.core.domain.credentials.passkey

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.ustadmobile.core.util.ext.formattedHost
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import com.ustadmobile.core.domain.credentials.PassKeySignInData
import com.ustadmobile.core.domain.credentials.CreatePasskeyRequestJsonUseCase
import com.ustadmobile.core.domain.credentials.passkey.webAuthn.ClientDataJSON
import com.ustadmobile.core.domain.credentials.passkey.webAuthn.PasskeyWebAuthNResponse
import com.ustadmobile.core.impl.config.SystemUrlConfig
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.json.JSONObject

class GetCredentialUseCaseImpl(
    private val context: Context,
    private val passkeyRequestJsonUseCase: CreatePasskeyRequestJsonUseCase,
    private val apiUrlConfig: SystemUrlConfig,
    private val json: Json,
) : GetCredentialUseCase {

    override suspend fun invoke(): GetCredentialUseCase.CredentialResult {
        val credentialManager = CredentialManager.create(context)
        val domain: String = Url(apiUrlConfig.systemBaseUrl).formattedHost()

        val getPasswordOption = GetPasswordOption()
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = passkeyRequestJsonUseCase.requestJsonForSignIn(domain)
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

                    if (authResponseJson != null) {
                        val parsedResponse = json.decodeFromString<PasskeyWebAuthNResponse>(authResponseJson)

                        val decodedBytes = Base64.decode(parsedResponse.response.clientDataJSON, Base64.DEFAULT)
                        val clientDataJson = json.decodeFromString<ClientDataJSON>(decodedBytes.decodeToString())

                        GetCredentialUseCase.PasskeyCredentialResult(
                            PassKeySignInData(
                                credentialId = parsedResponse.id,
                                userHandle = parsedResponse.response.userHandle?:"",
                                authenticatorData = parsedResponse.response.authenticatorData?:"",
                                clientDataJSON = parsedResponse.response.clientDataJSON,
                                signature = parsedResponse.response.signature?:"",
                                origin = clientDataJson.origin,
                                rpId = "credential-manager-${domain}",
                                challenge = clientDataJson.challenge
                            )
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