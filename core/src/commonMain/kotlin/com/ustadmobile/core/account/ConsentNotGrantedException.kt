package com.ustadmobile.core.account

/**
 * Authentication exception indicating that the
 */
class ConsentNotGrantedException(message: String? = null, cause: Throwable? = null) : IllegalStateException(message, cause)