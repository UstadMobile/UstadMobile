package com.ustadmobile.core.domain.validateusername

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource

/**
 * Validates whether a username meets all required criteria:
 * - Must not be too short or too long
 * - Must not start with a number
 * - Must only contain valid characters (letters, numbers, dots, underscores)
 */
data class ValidationResult(val errorMessage: StringResource? = null) {
    companion object {
        val Valid = ValidationResult()
        val TooShort = ValidationResult(MR.strings.username_too_short)
        val TooLong = ValidationResult(MR.strings.username_too_long)
        val StartsWithNumber = ValidationResult(MR.strings.username_starts_with_number)
        val InvalidCharacters = ValidationResult(MR.strings.invalid_username)
    }
}

class ValidateUsernameUseCase {
    operator fun invoke(username: String): ValidationResult {
        return when {
            username.length < MIN_LENGTH -> ValidationResult.TooShort
            username.length > MAX_LENGTH -> ValidationResult.TooLong
            username.firstOrNull()?.isDigit() == true -> ValidationResult.StartsWithNumber
            !username.all { isValidUsernameChar(it) } -> ValidationResult.InvalidCharacters
            else -> ValidationResult.Valid
        }
    }

    companion object {
        private const val MIN_LENGTH = 3
        private const val MAX_LENGTH = 30
        val ALLOWED_SPECIAL = setOf('.', '_')

        fun isValidUsernameChar(character: Char) = when {
            character.isLetter() -> true
            character.isDigit() -> true
            character in ALLOWED_SPECIAL -> true
            else -> false
        }
    }
}