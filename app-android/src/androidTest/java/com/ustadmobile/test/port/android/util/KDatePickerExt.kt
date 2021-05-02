package com.ustadmobile.test.port.android.util

import com.agoda.kakao.picker.date.KDatePicker
import java.util.*

fun KDatePicker.setDate(timeInMillis: Long, timeZoneId: String = TimeZone.getDefault().id) {
    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    cal.timeInMillis = timeInMillis
    setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH))
}
