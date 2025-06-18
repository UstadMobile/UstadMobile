package com.ustadmobile.core.util.report

import kotlinx.datetime.Month
import java.time.format.TextStyle
import java.util.Locale

actual fun getMonthDisplayName(month: Int): String {
    return Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
}