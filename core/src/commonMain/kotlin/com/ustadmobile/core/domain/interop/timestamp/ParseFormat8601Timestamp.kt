package com.ustadmobile.core.domain.interop.timestamp

expect fun format8601Timestamp(time: Long): String

expect fun parse8601Timestamp(timestamp: String): Long
