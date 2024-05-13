package com.ustadmobile.core.domain.interop.timestamp

import kotlin.js.Date


actual fun format8601Timestamp(time: Long): String {
    return Date(time).toISOString()
}

actual fun parse8601Timestamp(timestamp: String): Long {
    return Date(timestamp).getTime().toLong()
}
