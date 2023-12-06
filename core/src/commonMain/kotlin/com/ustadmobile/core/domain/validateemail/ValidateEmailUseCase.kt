package com.ustadmobile.core.domain.validateemail

class ValidateEmailUseCase() {

    /**
     * As per https://www.rfc-editor.org/rfc/rfc5322 section 3.4.1 ):
     *
     * Address must have at least one @ sign
     * Address must have at least one . after the @ sign
     * Must not have any spaces, [, ], or \
     *
     * @return if valid, return the valid email address (with any leading or trailing spaces trimmed). If invalid, return null
     */
    operator fun invoke(email: String): String? {
        val trimmed = email.trim()
        val atPos = trimmed.indexOf('@')

        //Must have at sign
        if(atPos == -1)
            return null


        //Must have at least one dot after the at sign
        if(trimmed.indexOf('.', atPos) == -1)
            return null

        if(trimmed.any { it.isWhitespace() || it == '[' || it == ']' || it == '\\' })
            return null

        return trimmed
    }

}