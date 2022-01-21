package com.ustadmobile.core.contentjob

/**
 * This exception can be used by ContentPlugin extractMetadata functions to tell ContentJobRunner
 * that no plugin is available to complete the job
 *
 * E.g. If zip file given that's not H5p or xapi
 */
class ContentTypeNotSupportedException(
        message: String? = null,
        cause: Throwable? = null
) : Exception(message, cause) {
}