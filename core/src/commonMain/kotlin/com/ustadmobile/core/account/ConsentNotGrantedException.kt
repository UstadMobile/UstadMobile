package com.ustadmobile.core.account

/**
 * Authentication exception indicating that the user is a minor and consent for use has not been
 * granted.
 */
class ConsentNotGrantedException(
    message: String? = null,
    cause: Throwable? = null
) : IllegalStateException(message, cause)