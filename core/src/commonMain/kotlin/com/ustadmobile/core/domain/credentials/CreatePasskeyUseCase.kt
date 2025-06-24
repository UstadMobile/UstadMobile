package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.domain.credentials.passkey.model.AuthenticationResponseJSON

interface CreatePasskeyUseCase {

    sealed class CreatePasskeyResult

    data class PasskeyCreatedResult(
        val authenticationResponseJSON : AuthenticationResponseJSON
    ) : CreatePasskeyResult()

    class UserCanceledResult : CreatePasskeyResult(){
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
    ) : CreatePasskeyResult()

    suspend operator fun invoke(username:String): CreatePasskeyResult

}