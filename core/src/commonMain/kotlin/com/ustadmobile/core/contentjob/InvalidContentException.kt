package com.ustadmobile.core.contentjob

/**
 * This exception is thrown when the content definitely SHOULD be a particular type e.g. because
 * the mime type matches, the file extension matches, etc. but the content itself is invalid e.g.
 * it is not actually that file type, it is corrupt, etc.
 *
 * This causes the contentimportersmanager to stop all processing and tell the user that the
 * content is invalid.
 */
class InvalidContentException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
