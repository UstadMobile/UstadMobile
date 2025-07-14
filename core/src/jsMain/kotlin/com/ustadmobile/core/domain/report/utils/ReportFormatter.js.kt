package com.ustadmobile.core.domain.report.utils

actual fun getMonthDisplayName(month: Int): String {
    return js("new Intl.DateTimeFormat('en-US', { month: 'long' }).format(new Date(2000, month-1, 1))") as String
}