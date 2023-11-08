package com.ustadmobile.core.domain.contententry.getmetadatafromuri

/**
 * Indicates that the uploaded content is not supported by the system. This is represented by
 * an http status code of 406 (Not Acceptable)
 */
class UnsupportedContentException(message: String): Exception(message)
