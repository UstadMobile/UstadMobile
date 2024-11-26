package com.ustadmobile.core.domain.validateusername


class ValidateUsernameUseCase {

    /**
     * Validates a username according to the following rules:
     * - Must not contain spaces, tabs, or banned special characters like !@#$%^&*()
     * - Non-English Unicode characters are allowed
     * - Must be 3–15 characters long after trimming
     *
     * @param username The username string to validate
     * @return The valid username (trimmed) if valid, or null if invalid
     */
    operator fun invoke(username: String): String? {
        val trimmed = username.trim()

        if (trimmed.length !in 3..15) {
            return null // Username must be between 3 and 15 characters
        }

        val bannedCharacters = "!@#$%^&*()[]{}|\\:;\"'<>,?/+=`~\t\n\r "

        if (trimmed.any { it in bannedCharacters }) {
            return null
        }

        return trimmed
    }
}
