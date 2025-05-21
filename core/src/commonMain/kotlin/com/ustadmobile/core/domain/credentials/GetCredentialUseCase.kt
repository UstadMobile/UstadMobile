package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON

/**
 * Use case to get a saved credential - passkey or username/password. This is a non-scoped
 * singleton (because there is one credential manager on the system).
 */
interface GetCredentialUseCase {

    sealed class CredentialResult

    /**
     * Result that represents a user selecting a saved username/password from credential
     * manager.
     *
     * @param credentialUsername saved username as per credentialUsernameForUserAndLearningSpace e.g.
     *        username@learningspace.example.org
     * @param password account saved password.
     */
    data class PasswordCredentialResult(
        val credentialUsername: String,
        val password: String
    ) : CredentialResult()

    /**
     * Passkey result
     */
    data class PasskeyCredentialResult(
        val passkeyWebAuthNResponse: AuthenticationResponseJSON
    ) : CredentialResult()

    data class Error(
        val message: String?
    ) : CredentialResult()

    suspend operator fun invoke(): CredentialResult

}