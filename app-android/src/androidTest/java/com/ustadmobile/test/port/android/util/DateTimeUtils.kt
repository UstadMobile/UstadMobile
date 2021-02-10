package com.ustadmobile.test.port.android.util

import android.widget.DatePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.picker.date.KDatePicker
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import java.util.*


/**
 * Utility function to set a datefield that uses our two-way data binding
 */
@Deprecated("Use KEditTextExt.setDateWithDialog instead")
fun setDateField(viewId: Int, timeInMillis: Long, timeZoneId: String = "UTC") {
    KView {
        withId(viewId)
    } perform {
        click()
    }

    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    cal.timeInMillis = timeInMillis

    KDatePicker {
        withClassName(equalTo(DatePicker::class.java.name))
    } perform {
        setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))

        hasDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
    }
    KView {
        withId(android.R.id.button1)
    } perform {
        click()
    }
}