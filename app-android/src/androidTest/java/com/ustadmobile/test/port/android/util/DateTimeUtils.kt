package com.ustadmobile.test.port.android.util

import android.widget.DatePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matchers
import java.util.*


/**
 * Utility function to set a datefield that uses our two-way data binding
 */
fun setDateField(viewId: Int, timeInMillis: Long, timeZoneId: String = "UTC") {
    Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.click())
    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    cal.timeInMillis = timeInMillis

    Espresso.onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)))
    Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click())
}