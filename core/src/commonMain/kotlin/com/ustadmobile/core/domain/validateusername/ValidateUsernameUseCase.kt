package com.ustadmobile.core.domain.validateusername

class ValidateUsernameUseCase {
    /**
     * Validates username according to rules:
     * - Must not contain special characters (except . and _)
     * - Must not start with a number
     * - All letters will be converted to lowercase
     * - Any banned characters will be replaced with provided replacement string
     * @param username The username string to validate
     */
    operator fun invoke(
        username: String,
        invalidReplacement: String = ""
    ): String? {
        val trimmed = username.trim()
        if(trimmed.isEmpty()) return null

        val lowercased = trimmed.lowercase()

        // Return null if starts with number
        if(lowercased.firstOrNull()?.isDigit() == true) {
            return null
        }

        // Replace any invalid characters with replacement
        val result = lowercased.map { char ->
            if(isCharacterAllowed(char)) {
                char.toString()
            } else {
                invalidReplacement
            }
        }.joinToString("")

        return result.ifEmpty { null }
    }

    /**
     * Check if character is allowed in username
     * @param char Character to check
     * @param isFirstChar Whether this is first character being typed
     * @return true if character is allowed, false otherwise
     */
    fun isCharacterAllowed(char: Char, isFirstChar: Boolean = false): Boolean {
        return when {
            char.isLetter() -> true
            char in ALLOWED_SPECIAL -> true
            char.isDigit() -> !isFirstChar
            else -> false
        }
    }

    companion object {
        private val ALLOWED_SPECIAL = setOf('.', '_')
    }
}