package com.ustadmobile.core.util.ext

import kotlin.js.Date

actual fun Long.formatDate(context: Any): String {
    val date = Date(this)
    return "${date.getDate()}-${date.getMonth()}-${date.getFullYear()}"
}