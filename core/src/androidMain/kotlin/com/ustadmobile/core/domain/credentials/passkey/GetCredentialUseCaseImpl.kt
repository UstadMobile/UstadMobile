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
import com.ustadmobile.core.domain.credentials.CreatePasskeyRequestJsonUseCase
import com.ustadmobile.core.util.ext.formattedHost
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import com.ustadmobile.core.domain.credentials.PassKeySignInData
import com.ustadmobile.core.impl.config.SystemUrlConfig
import io.ktor.http.Url
import org.json.JSONObject

class GetCredentialUseCaseImpl(
    private val context: Context,
    private val passkeyRequestJsonUseCase: CreatePasskeyRequestJsonUseCase,
    private val apiUrlConfig: SystemUrlConfig,
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
                    val authResponseJson = credential.data.getString(
                        "androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON"
                    )

                    if (authResponseJson != null) {
                        val jsonObject = JSONObject(authResponseJson)
                        val responseObject = jsonObject.getJSONObject("response")

                        val clientDataJsonString = responseObject.getString("clientDataJSON")
                        val decodedBytes = Base64.decode(clientDataJsonString, Base64.DEFAULT)
                        val decodedJson = String(decodedBytes)
                        val clientDataJson = JSONObject(decodedJson)

                        GetCredentialUseCase.PasskeyCredentialResult(
                            PassKeySignInData(
                                credentialId = jsonObject.getString("id"),
                                userHandle = responseObject.getString("userHandle"),
                                authenticatorData = responseObject.getString("authenticatorData"),
                                clientDataJSON = clientDataJsonString,
                                signature = responseObject.getString("signature"),
                                origin = clientDataJson.getString("origin"),
                                rpId = "credential-manager-${domain}",
                                challenge = clientDataJson.getString("challenge")
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