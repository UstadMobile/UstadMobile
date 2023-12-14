package com.ustadmobile.core.domain.validateusername

class ValidateUsernameUseCase {
    operator fun invoke(username: String): String? {

        if (username.any { it.isUpperCase() }){
            return null
        }

        return username
    }
}