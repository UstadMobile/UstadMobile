package com.ustadmobile.core.domain.ValidateUsername

class ValidateUsernameUseCase {
    operator fun invoke(username: String): Boolean{
        var isValid = true

        if (username.isEmpty()) {
            isValid = false
        }

        if (isValid) {
            if (username.contains(" ")) {
                isValid = false
            }
        }

        if (isValid) {
            val usernameChars = username.toCharArray()

            for (i in 1..<usernameChars.count()) {
                if (usernameChars[i].isUpperCase()) {
                    isValid = false
                }
            }
        }

        return isValid
    }
}