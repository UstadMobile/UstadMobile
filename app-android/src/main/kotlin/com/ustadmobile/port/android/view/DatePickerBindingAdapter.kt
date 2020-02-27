package com.ustadmobile.port.android.view

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.ustadmobile.core.util.UMCalendarUtil
import java.util.*

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

@BindingAdapter("selectDate")
fun bindDateClicks(editText: EditText, date: Long) {
    editText.isFocusable = false
    if (date == 0L) {
        editText.setText("-")
    }

    editText.setOnClickListener {
        openDatePicker(editText, editText.context, date)
    }
}

fun openDatePicker(editText: EditText, context: Context, date: Long) {
    val c = Calendar.getInstance()

    //date listener - opens a new date picker.
    val startDateListener = { _: DatePicker, year: Int, month:Int, dayOfMonth: Int ->
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val newDate = c.timeInMillis

        editText.setText(UMCalendarUtil.getPrettyDateFromLong(newDate, Locale.getDefault())?:"")

    }

    val datePicker = DatePickerDialog(
            context, startDateListener, c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH))
    datePicker.show()

}

class DatePickerBindingAdapter {

    private var dateString = ""

}