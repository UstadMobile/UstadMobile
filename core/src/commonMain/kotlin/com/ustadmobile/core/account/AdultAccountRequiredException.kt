package com.ustadmobile.core.account

/**
 * Indicates that an adult account is required for the given action, but login was detected for a
 * child account.
 */
class AdultAccountRequiredException(message: String? = null, cause: Throwable? = null) : IllegalStateException(message, cause)