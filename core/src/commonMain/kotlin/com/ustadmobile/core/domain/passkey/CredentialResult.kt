package com.ustadmobile.core.domain.passkey

sealed class CredentialResult {
    data class PasswordCredentialResult(
        val username: String?,
        val password: String?
    ) : CredentialResult()

    data class PasskeyCredentialResult(
        val passKeySignInData: PassKeySignInData
    ) : CredentialResult()

    data class Error(
        val message: String?
    ) : CredentialResult()
}