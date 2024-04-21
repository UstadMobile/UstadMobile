package com.ustadmobile.lib.rest.mediahelpers

/**
 * Exception that can be thrown by the Rest application to indicate that FFMPEG is not installed.
 * ServerAppMain may then handle downloading it for the user.
 */
class MissingMediaProgramsException(message: String): Exception(message)

