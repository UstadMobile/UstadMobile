package com.ustadmobile.core.util.ext

import java.text.DateFormat
import java.util.*

actual fun Long.formatDate(context: Any): String {
    return DateFormat.getDateInstance(DateFormat.SHORT).format(Date(this))
}
