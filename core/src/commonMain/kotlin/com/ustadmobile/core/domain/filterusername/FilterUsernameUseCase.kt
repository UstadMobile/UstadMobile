package com.ustadmobile.core.domain.filterusername

import com.ustadmobile.core.domain.validateusername.ValidateUsernameUseCase

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
                char.isDigit() -> char
                char in VALID_USERNAME_SPECIAL_CHARS -> char
                else -> invalidCharReplacement
            }
        }.joinToString("")
    }

    companion object {
        private val VALID_USERNAME_SPECIAL_CHARS = ValidateUsernameUseCase.ALLOWED_SPECIAL
    }
}