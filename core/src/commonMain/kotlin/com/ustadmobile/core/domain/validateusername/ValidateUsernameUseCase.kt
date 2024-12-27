package com.ustadmobile.core.domain.validateusername

/**
 * Validates whether a username meets all required criteria:
 * - Must not be too short or too long
 * - Must not start with a number
 * - Must only contain valid characters (letters, numbers, dots, underscores)
 */
class ValidateUsernameUseCase {

    enum class ValidationResult {
        VALID,
        INVALID_TOO_SHORT,
        INVALID_TOO_LONG,
        INVALID_STARTS_WITH_NUMBER,
        INVALID_OTHER
    }

    operator fun invoke(username: String): ValidationResult {
        return when {
            username.length < MIN_LENGTH -> ValidationResult.INVALID_TOO_SHORT
            username.length > MAX_LENGTH -> ValidationResult.INVALID_TOO_LONG
            username.firstOrNull()?.isDigit() == true -> ValidationResult.INVALID_STARTS_WITH_NUMBER
            !username.all { it.isValidUsernameChar() } -> ValidationResult.INVALID_OTHER
            else -> ValidationResult.VALID
        }
    }

    private fun Char.isValidUsernameChar(): Boolean = when {
        isLetter() -> true
        isDigit() -> true
        this in ALLOWED_SPECIAL -> true
        else -> false
    }

    companion object {
        private const val MIN_LENGTH = 3
        private const val MAX_LENGTH = 30
        private val ALLOWED_SPECIAL = setOf('.', '_')
    }
}