package com.ustadmobile.libcache.date

import io.ktor.http.fromHttpToGmtDate

fun String.fromHttpDateToMillis(): Long {
    return fromHttpToGmtDate().timestamp
}
