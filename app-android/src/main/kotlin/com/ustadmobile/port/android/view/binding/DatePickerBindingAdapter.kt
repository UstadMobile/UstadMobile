package com.ustadmobile.port.android.view.binding

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

var updatedDate = 0L
var etToListener = mutableMapOf<Int, InverseBindingListener>()

fun updateDateOnEditText(et: EditText, date: Long) {
    val dateFormatter: SimpleDateFormat by lazy { SimpleDateFormat("EEE, dd/MMM/yyyy",
            Locale(UstadMobileSystemImpl.instance.getLocale(et.context)?:"")) }
    if (date == 0L) {
        et.setText("-")
    }else{
        et.setText(dateFormatter.format(date))
    }
}

fun openDatePicker(et: EditText, context: Context, date: Long) {
    val c = Calendar.getInstance()
    if(date > 0) {
        c.timeInMillis = date
    }

    //date listener - opens a new date picker.
    val startDateListener = { _: DatePicker, year: Int, month:Int, dayOfMonth: Int ->
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updatedDate = c.timeInMillis

        etToListener[et.id]?.onChange()

        val a=""
    }

    val datePicker = DatePickerDialog(
            context, startDateListener, c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH))
    datePicker.show()
}


@BindingAdapter("realValueAttrChanged")
fun getDate(et: EditText, inverseBindingListener: InverseBindingListener){
    etToListener[et.id] = inverseBindingListener

}

@BindingAdapter("realValue")
fun setDate(et: EditText, date: Long){
    updateDateOnEditText(et, date)
    et.setOnClickListener {
        openDatePicker(et, et.context, date)
    }
}

@InverseBindingAdapter(attribute = "realValue")
fun getRealValue(et: EditText): Long {
    //etToListener.remove(et.id)
    updateDateOnEditText(et, updatedDate)
    return updatedDate
}

class DatePickerBindingAdapter {

}