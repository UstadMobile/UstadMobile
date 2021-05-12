package com.ustadmobile.core.util.ext

import android.content.Context
import android.text.format.DateFormat
import java.util.*

actual fun Long.formatDate(context: Any): String {
    return DateFormat.getDateFormat(context as Context).format(Date(this))
}
