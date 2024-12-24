package com.ustadmobile.libuicompose.util.password

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.ustadmobile.core.domain.password.LoginWithSavePasswordResult
import com.ustadmobile.core.domain.password.LoginWithSavedPasswordUseCase

class LoginWithSavedPasswordUseCaseImpl(
    val context: Context
) : LoginWithSavedPasswordUseCase {
    override suspend fun invoke(): LoginWithSavePasswordResult {
         try {
            val credentialManager = CredentialManager.create(context)

            val credentialResponse = credentialManager.getCredential(
                context = context,
                request = GetCredentialRequest(
                    credentialOptions = listOf(GetPasswordOption())
                )
            )

            val credential = credentialResponse.credential as? PasswordCredential
            return LoginWithSavePasswordResult(
                username = credential?.id,
                password = credential?.password)
        } catch (e: NoCredentialException) {
            return LoginWithSavePasswordResult(error = e.message)
        } catch (e: GetCredentialException) {
             return LoginWithSavePasswordResult(error = e.message)
        }
    }

}