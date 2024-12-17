package com.ustadmobile.core.domain.validateusername

class ValidateUsernameUseCase {
    /**
     * Validates username according to rules:
     * - Must not contain spaces, tabs, or special characters (except . and _)
     * - Must not start with a number
     * - Non-English Unicode characters are allowed
     * - Must be 3–15 characters long after trimming
     * - All letters will be converted to lowercase
     *
     * @param username The username string to validate
     * @param invalidReplacement String to replace invalid characters with (default empty string)
     * @return The valid username if valid, or null if invalid
     */
    operator fun invoke(
        username: String,
        invalidReplacement: String = ""
    ): String? {
        val trimmed = username.trim().lowercase()

        if (trimmed.length !in MIN_LENGTH..MAX_LENGTH) {
            return null
        }

        if (trimmed.firstOrNull()?.isDigit() == true) {
            return null
        }

        val containsInvalidChars = trimmed.any { !isCharacterAllowed(it) }
        if (containsInvalidChars) {
            if (invalidReplacement.isEmpty()) return null

            return trimmed.map { char ->
                if (!isCharacterAllowed(char)) invalidReplacement else char
            }.joinToString("")
        }

        return trimmed
    }

    /**
     * Check if character is allowed in username for keyboard input
     * @param char Character to check
     * @param isFirstChar Whether this is first character being typed
     * @return true if character is allowed
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
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 15
        private val ALLOWED_SPECIAL = setOf('.', '_')
    }
}