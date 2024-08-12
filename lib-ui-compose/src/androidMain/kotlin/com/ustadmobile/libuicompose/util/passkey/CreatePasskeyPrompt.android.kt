package com.ustadmobile.libuicompose.util.passkey

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import com.ustadmobile.core.domain.passkey.PasskeyResult
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.util.ext.getActivityContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * the CreatePasskeyPrompt will show the google bottomsheet to create passkey
 * https://developer.android.com/identity/sign-in/credential-manager#create-passkey
 */
@Composable
actual fun CreatePasskeyPrompt(
    username: String,
    personUid: String,
    doorNodeId: String,
    usStartTime: Long,
    serverUrl:String,
    passkeyData: (PasskeyResult) -> Unit,
    passkeyError: (String) -> Unit
) {
    val activity = LocalContext.current.getActivityContext()
    val credentialManager = CredentialManager.create(activity)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(username, personUid, doorNodeId, usStartTime) {
        coroutineScope.launch {
            val passkey = createPasskey(activity, credentialManager, username, personUid, doorNodeId, usStartTime, passkeyError,serverUrl)
            if (passkey != null) {
                passkeyData(passkey)
            }
        }
    }
}

@SuppressLint("PublicKeyCredential")
private suspend fun createPasskey(
    activity: Activity,
    credentialManager: CredentialManager,
    username: String,
    personUid: String,
    doorNodeId: String,
    usStartTime: Long,
    passkeyError: (String) -> Unit,
    serverUrl:String
): PasskeyResult? {

    val request = CreatePublicKeyCredentialRequest(
        PasskeyRequestJsonUseCase.createPasskeyRequestJson(
            username,
            personUid,
            doorNodeId,
            usStartTime,
            serverUrl
        )
    )
    var passkeyResult: PasskeyResult? = null
    try {
        val response = credentialManager.createCredential(
            activity,
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
            personUid = personUid.toLong(),
            person = Person()
        )
    } catch (e: CreateCredentialException) {
        Napier.e(e) { "Failed to create passkey: ${e.message}" }
        passkeyError(e.message ?: "error occurred while creating the passkey")
    }

    return passkeyResult
}
