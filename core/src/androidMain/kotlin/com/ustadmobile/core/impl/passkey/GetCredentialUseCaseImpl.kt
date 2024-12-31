package com.ustadmobile.core.impl.passkey

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
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.passkey.CredentialResult
import com.ustadmobile.core.domain.passkey.GetCredentialUseCase
import com.ustadmobile.core.domain.passkey.PassKeySignInData
import com.ustadmobile.core.domain.passkey.PasskeyRequestJsonUseCase
import io.ktor.http.Url
import org.json.JSONObject

class GetCredentialUseCaseImpl(
    val context: Context,
    val learningSpace: LearningSpace,
    val passkeyRequestJsonUseCase: PasskeyRequestJsonUseCase
) : GetCredentialUseCase {
    private val domain: String by lazy {
        Url(learningSpace.url).host
    }
    override suspend fun invoke(): CredentialResult {
        val credentialManager = CredentialManager.create(context)

        val getPasswordOption = GetPasswordOption()
        val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
            requestJson = passkeyRequestJsonUseCase.requestJsonForSignIn(domain)
        )

        val getCredentialRequest = GetCredentialRequest(
            credentialOptions = listOf(getPasswordOption, getPublicKeyCredentialOption),
            preferImmediatelyAvailableCredentials = true
        )

        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = getCredentialRequest
            )

            when (val credential = result.credential) {
                is PasswordCredential -> {
                    CredentialResult.PasswordCredentialResult(
                        username = credential.id,
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

                        CredentialResult.PasskeyCredentialResult(
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
                        CredentialResult.Error("Auth response JSON is null.")
                    }
                }

                else -> {
                    CredentialResult.Error("Unknown credential type.")
                }
            }
        } catch (e: NoCredentialException) {
            CredentialResult.Error("No credentials found: ${e.message}")
        } catch (e: GetCredentialException) {
            CredentialResult.Error("Failed to get credential: ${e.message}")
        }
    }
}