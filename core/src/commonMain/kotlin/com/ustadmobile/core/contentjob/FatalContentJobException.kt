package com.ustadmobile.core.contentjob

/**
 * This exception can be used by ContentPlugin processJob functions to tell ContentJobRunner NOT
 * to retry.
 *
 * E.g. if the sourceUrl has a fatal http exception (e.g. 404), there is no point in retrying.
 */
class FatalContentJobException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
