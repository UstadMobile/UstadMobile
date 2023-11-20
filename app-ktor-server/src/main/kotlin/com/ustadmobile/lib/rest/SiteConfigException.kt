package com.ustadmobile.lib.rest

/**
 * Exception that indicates something is wrong with the configuration. The message will be output
 * to the console without the stacktrace.
 */
class SiteConfigException(message: String): Exception(message)
