package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON

interface CreatePasskeyUseCase {

    sealed class CreateCredentialResult()

    data class CreatePasskeyResult(
        val authenticationResponseJSON : AuthenticationResponseJSON
    ) : CreateCredentialResult()

    class UserCanceledResult : CreateCredentialResult(){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GetCredentialUseCase.UserCanceledResult) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class Error(
        val message: String?
    ) : CreateCredentialResult()

    suspend operator fun invoke(username:String): CreateCredentialResult

}