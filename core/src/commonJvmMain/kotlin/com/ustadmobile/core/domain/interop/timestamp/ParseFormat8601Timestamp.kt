package com.ustadmobile.core.domain.interop.timestamp

import java.time.Instant
import java.time.format.DateTimeFormatter

actual fun format8601Timestamp(time: Long): String {
    val instant = Instant.ofEpochMilli(time)
    return DateTimeFormatter.ISO_INSTANT.format(instant)
}

actual fun parse8601Timestamp(timestamp: String): Long {
    val temporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(timestamp)
    val instant = Instant.from(temporalAccessor)
    return instant.toEpochMilli()
}
