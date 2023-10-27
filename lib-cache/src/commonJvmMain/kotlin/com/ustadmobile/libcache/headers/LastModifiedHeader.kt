package com.ustadmobile.libcache.headers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * As per
 * https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
 */
val LAST_MODIFIED_FORMATTER = SimpleDateFormat(
    "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US
).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}

actual fun lastModifiedHeader(
    time: Long
): HttpHeader {
    return HttpHeader(
        name = "Last-Modified",
        value = LAST_MODIFIED_FORMATTER.format(Date(time))
    )
}
