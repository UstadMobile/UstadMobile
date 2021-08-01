package com.ustadmobile.core.account

/**
 * Authentication exception indicating that the
 */
class AccountNotApprovedException(message: String? = null, cause: Throwable? = null) : IllegalStateException(message, cause)