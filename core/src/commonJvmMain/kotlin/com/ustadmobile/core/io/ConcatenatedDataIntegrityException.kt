package com.ustadmobile.core.io

import java.io.IOException

/**
 * IOException subclass used to indicate a data integrity issue
 */
class ConcatenatedDataIntegrityException(message: String, cause: Throwable? = null) : IOException(message, cause) {
}