package com.ustadmobile.core.domain.tmpfiles

/**
 * Determines if the given uri is a temporary file. When a file is selected for processing (e.g. a
 * photo), it might be a temporary file (e.g. saved by the camera app) or it might be a uri
 * returned from the file browser.
 *
 */
interface IsTempFileCheckerUseCase {

    operator fun invoke(uri: String): Boolean

}