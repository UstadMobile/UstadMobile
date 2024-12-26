package com.ustadmobile.core.domain.filterusername

/**
 * Filters and normalizes username input:
 * - Converts uppercase to lowercase
 * - Filters invalid characters
 * - Handles key event validation
 */
class FilterUsernameUseCase {
    operator fun invoke(
        username: String,
        invalidCharReplacement: Char
    ): String {
        return username.map { char ->
            when {
                char.isLetter() -> char.lowercase()
                char in VALID_USERNAME_SPECIAL_CHARS -> char
                char.isDigit() && username.firstOrNull() != char -> char
                else -> invalidCharReplacement
            }
        }.joinToString("")
    }

    companion object {
        private val VALID_USERNAME_SPECIAL_CHARS = setOf('.', '_')

        fun shouldBlockKeyEvent(char: Char): Boolean = when {
            char.isWhitespace() -> true
            char.isLetter() -> false
            char in VALID_USERNAME_SPECIAL_CHARS -> false
            char.isDigit() -> false
            else -> true
        }
    }
}