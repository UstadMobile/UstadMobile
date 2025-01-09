package com.ustadmobile.core.impl.password

import com.ustadmobile.core.domain.password.SavePasswordUseCase
import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialNoCreateOptionException
import com.ustadmobile.core.account.LearningSpace
import io.ktor.http.Url

class SavePasswordUseCaseImpl(
    val context: Context,
    val learningSpace: LearningSpace,
) : SavePasswordUseCase {
    private val domain: String by lazy {
        Url(learningSpace.url).host
    }
    override suspend fun invoke(username: String, password: String) {
        val credentialManager = CredentialManager.create(context)
        try {
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(
                    id = username,
                    password = password,
                    origin = domain
                )
            )
            print("Password saved successfully for user: $username")
        } catch (e: CreateCredentialNoCreateOptionException) {
            e.printStackTrace()
        } catch (e: CreateCredentialException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
