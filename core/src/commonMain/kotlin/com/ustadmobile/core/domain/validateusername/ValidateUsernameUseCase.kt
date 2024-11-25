package com.ustadmobile.core.domain.validateusername


class ValidateUsernameUseCase {

    /**
     * Validates a username according to the following rules:
     * - Must not contain spaces, numbers, or special characters except . and _
     * - Non-English Unicode characters are allowed
     * - Cannot be empty
     *
     * @param username The username string to validate
     * @return The valid username (trimmed) if valid, or null if invalid
     */
    operator fun invoke(username: String): String? {
        val trimmed = username.trim()

        // Must not be empty
        if (trimmed.isEmpty()) return null

        // Regex: Allows letters, . and _; disallows spaces and numbers
        val regex = "^[\\p{L}._]+\$".toRegex()

        return if (regex.matches(trimmed)) trimmed else null
    }
}
