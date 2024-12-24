package com.ustadmobile.libuicompose.util.password

import com.ustadmobile.core.domain.password.SavePasswordUseCase
import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import io.github.aakira.napier.Napier

class SavePasswordUseCaseImpl(
    val context: Context
):SavePasswordUseCase{
    override suspend fun invoke(username: String, password: String) {
        val credentialManager = CredentialManager.create(context)
        try {
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(
                    id = username,
                    password = password
                )
            )
            Napier.d { "Password saved successfully for user: $username" }
        } catch (e: CreateCredentialNoCreateOptionException) {
            Napier.w { "No option to create credentials: ${e.message}" }
        } catch (e: CreateCredentialException) {
            Napier.e { "Error saving credentials: ${e.message}" }
        } catch (e: Exception) {
            Napier.e { "Unexpected error: ${e.message}" }
        }
    }

}
