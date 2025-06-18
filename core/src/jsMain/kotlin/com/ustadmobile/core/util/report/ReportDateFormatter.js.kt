package com.ustadmobile.core.util.report

actual fun getMonthDisplayName(month: Int): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return monthNames.getOrNull(month - 1) ?: month.toString()
}
