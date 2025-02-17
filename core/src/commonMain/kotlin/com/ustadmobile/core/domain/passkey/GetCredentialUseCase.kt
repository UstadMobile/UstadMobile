package com.ustadmobile.core.domain.passkey

interface GetCredentialUseCase {
    suspend operator fun invoke(systemBaseUrl: String): CredentialResult

}