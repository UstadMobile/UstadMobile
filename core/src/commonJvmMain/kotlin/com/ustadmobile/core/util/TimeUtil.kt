package com.ustadmobile.core.util

import java.time.Duration

actual fun parse8601Duration(duration: String): Long {
    return Duration.parse(duration).toMillis()
}
