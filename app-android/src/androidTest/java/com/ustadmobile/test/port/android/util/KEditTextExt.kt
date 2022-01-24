package com.ustadmobile.test.port.android.util

import android.R
import android.widget.DatePicker
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.picker.date.KDatePicker
import org.hamcrest.CoreMatchers
import java.util.*

/**
 * Convenience function to set the date on a EditText which is linked to a Date popup (e.g. using
 * the two-way data binding).
 */
fun KEditText.setDateWithDialog(timeInMillis: Long, timeZoneId: String = TimeZone.getDefault().id) {
    click()

    KDatePicker {
        withClassName(CoreMatchers.equalTo(DatePicker::class.java.name))
    } perform {
        setDate(timeInMillis, timeZoneId)
    }

    KView {
        withId(R.id.button1)
    } perform {
        click()
    }
}