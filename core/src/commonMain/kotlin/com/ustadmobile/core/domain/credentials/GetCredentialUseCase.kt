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

    /**
     * No credentials are available: on Android this is represented as an error, but it's not really
     * an error.
     */
    class NoCredentialAvailableResult: CredentialResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NoCredentialAvailableResult) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class Error(
        val message: String?
    ) : CredentialResult()

    suspend operator fun invoke(): CredentialResult

}