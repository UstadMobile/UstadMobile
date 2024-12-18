package com.ustadmobile.core.domain.validateusername

class ValidateUsernameUseCase {
    /**
     * Validates username according to rules:
     * - Must not contain special characters (except . and _)
     * - Must not start with a number
     * - All letters will be converted to lowercase
     * - Returns cleaned username or null if invalid
     *
     * @param username The username string to validate
     * @param invalidReplacement String to replace invalid characters with (default empty string)
     * @return The valid username if valid, or null if invalid
     */
    operator fun invoke(
        username: String,
        invalidReplacement: String = ""
    ): String? {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return null

        // Convert to lowercase
        val lowercased = trimmed.lowercase()

        // Check if starts with number
        if (lowercased.firstOrNull()?.isDigit() == true) {
            return null
        }

        // Check for invalid characters
        val containsInvalidChars = lowercased.any { !isCharacterAllowed(it) }
        if (containsInvalidChars && invalidReplacement.isEmpty()) {
            return null
        }

        // Replace invalid characters if replacement provided
        return if (containsInvalidChars) {
            lowercased.map { char ->
                when {
                    !isCharacterAllowed(char) -> invalidReplacement
                    char.isWhitespace() -> invalidReplacement
                    else -> char
                }
            }.joinToString("")
        } else {
            lowercased.replace("\\s+".toRegex(), invalidReplacement)
        }

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
        private val ALLOWED_SPECIAL = setOf('.', '_')
    }
}