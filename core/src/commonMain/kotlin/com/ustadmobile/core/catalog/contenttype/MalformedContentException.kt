package com.ustadmobile.core.catalog.contenttype

/**
 * This exception is thrown when the content definitely SHOULD be a particular type e.g. because
 * the mime type matches, the file extension matches, etc. but the content itself is invalid e.g.
 * it is not actually that file type, it is corrupt, etc.
 *
 * This causes the pluginmanager to stop all processing and tell the user that the content is
 * corrupt.
 */
class MalformedContentException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
