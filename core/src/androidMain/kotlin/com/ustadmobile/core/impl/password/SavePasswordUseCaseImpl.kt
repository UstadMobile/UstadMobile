package com.ustadmobile.core.impl.password

import com.ustadmobile.core.domain.password.SavePasswordUseCase
import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import com.ustadmobile.core.util.ext.formattedHost
import io.github.aakira.napier.Napier
import io.ktor.http.Url

class SavePasswordUseCaseImpl(
    val context: Context
):SavePasswordUseCase{
    override suspend fun invoke(username: String, password: String,learningSpace: String) {
        val credentialManager = CredentialManager.create(context)
        val domain = Url(learningSpace).formattedHost()
        try {
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(
                    id = "$username@$domain",
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
