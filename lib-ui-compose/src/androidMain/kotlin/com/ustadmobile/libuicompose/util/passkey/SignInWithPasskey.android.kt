package com.ustadmobile.libuicompose.util.passkey

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.GetCredentialException
import com.ustadmobile.core.domain.passkey.PassKeySignInData
import com.ustadmobile.libuicompose.util.ext.getActivityContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
actual fun SignInWithPasskey(
    onSignInWithPasskey: (PassKeySignInData) -> Unit,
) {
    val activity = LocalContext.current.getActivityContext()
    val credentialManager = CredentialManager.create(activity)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val getPasswordOption = GetPasswordOption()

            // Get passkey from the user's public key credential provider.
            val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
                requestJson = PasskeyRequestJsonUseCase.requestJsonForSignIn()
            )
            val getCredRequest = GetCredentialRequest(
                listOf(getPasswordOption, getPublicKeyCredentialOption)
            )

            try {
                val result = credentialManager.getCredential(
                    context = activity,
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
                    val passKeySignInData = PassKeySignInData(
                        credentialId = jsonObject.getString("id"),
                        userHandle = responseObject.getString("userHandle"),
                        authenticatorData = responseObject.getString("authenticatorData"),
                        clientDataJSON = clientDataJsonString,
                        signature = responseObject.getString("signature"),
                        origin = clientDataJson.getString("origin"),
                        rpId = "credential-manager-learningtree.ustadmobile.com",  // Replace with the actual rpId if needed.
                        challenge = clientDataJson.getString("challenge"),
                    )

                    // Trigger the callback with the created PassKeySignInData.
                    onSignInWithPasskey(passKeySignInData)
                } else {
                    Napier.e { "Auth response JSON is null." }
                }

            } catch (e: GetCredentialException) {
                Log.e("getCredential", "getCredential failed with exception message: ${e.message}")
                Napier.e(e) { "GetCredentialException: ${e.message}" }
            }
        }
    }
}



