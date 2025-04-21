package com.ustadmobile.core.domain.credentials.password

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SavePasswordUseCaseImpl(
    val context: Context
): SavePasswordUseCase {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override suspend fun invoke(
        username: String, password: String, learningSpace: String
    ) {
        /* Credential manager can take time, so run it in a coroutine on a separate scope to the
         * view model so that it won't keep the user waiting.
         */
        scope.launch {
            val credentialManager = CredentialManager.create(context)
            try {
                credentialManager.createCredential(
                    context = context,
                    request = CreatePasswordRequest(
                        id = GetCredentialUseCase.credentialUsernameForUserAndLearningSpace(
                            username = username,
                            learningSpace = LearningSpace(learningSpace),
                        ),
                        password = password,
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

}
