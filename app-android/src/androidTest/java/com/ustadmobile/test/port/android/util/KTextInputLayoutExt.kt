package com.ustadmobile.test.port.android.util

import android.widget.DatePicker
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.picker.date.KDatePicker
import org.hamcrest.CoreMatchers
import java.util.*

fun KTextInputLayout.setDateField(timeInMillis: Long, timeZoneId: String = "UTC") {

    click()

    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    cal.timeInMillis = timeInMillis

    KDatePicker {
        withClassName(CoreMatchers.equalTo(DatePicker::class.java.name))
    } perform {
        setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH))

        hasDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH))
    }
    KView {
        withId(android.R.id.button1)
    } perform {
        click()
    }

}