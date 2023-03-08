package com.ustadmobile.core.api.oneroster

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")

actual fun format8601Timestamp(time: Long): String {
    return dateFormat.format(Date(time))
}

actual fun parse8601Timestamp(timestamp: String): Long {
    return dateFormat.parse(timestamp).time
}
