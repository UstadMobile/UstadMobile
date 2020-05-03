package com.ustadmobile.port.android.view.binding

import android.app.DatePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import java.util.*

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditText(et: EditText, date: Long) {
    val dateFormatter = DateFormat.getDateFormat(et.context)
    if (date == 0L) {
        et.setText("")
    }else{
        et.setText(dateFormatter.format(date))
    }
}

fun openDatePicker2(et: EditText, context: Context, inverseBindingListener: InverseBindingListener) {
    val c = Calendar.getInstance()
    val currentDate = et.getTag(R.id.tag_datelong) as? Long ?: 0L
    if(currentDate > 0) {
        c.timeInMillis = currentDate
    }

    //date listener - opens a new date picker.
    val dateListener = { _: DatePicker, year: Int, month:Int, dayOfMonth: Int ->
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        et.setTag(R.id.tag_datelong, c.timeInMillis)
        updateDateOnEditText(et, c.timeInMillis)
        inverseBindingListener.onChange()
    }

    //see https://stackoverflow.com/questions/44418149/cant-get-android-datepickerdialog-to-switch-to-spinner-mode
    val datePicker = if(et.getTag(R.id.tag_dateusespinner) == true) {
        DatePickerDialog(
                ContextThemeWrapper(context, R.style.CustomDatePickerDialogTheme), R.style.CustomDatePickerDialogTheme, dateListener, c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH))
    }else {
        DatePickerDialog(
                context, dateListener, c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH))
    }
    datePicker.show()
}


@BindingAdapter("dateLongAttrChanged")
fun getDate(et: EditText, inverseBindingListener: InverseBindingListener){
    et.setOnClickListener {
        openDatePicker2(et, et.context,  inverseBindingListener)
    }
}
@BindingAdapter("dateLongStringAttrChanged")
fun getDateString(et: EditText, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openDatePicker2(et, et.context,  inverseBindingListener)
    }
}

@BindingAdapter("dateLong")
fun setDate(et: EditText, date: Long){
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

/**
 * Wrapper to handle when the result of the picker is stored on a string (e.g. CustomFieldValue)
 */
@BindingAdapter("dateLongString")
fun setDateString(et: EditText, dateLongString: String?){
    val date = dateLongString?.toLong() ?: 0L
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@InverseBindingAdapter(attribute = "dateLong")
fun getRealValue(et: EditText): Long {
    return et.getTag(R.id.tag_datelong) as? Long ?: 0L
}

@InverseBindingAdapter(attribute = "dateLongString")
fun getRealStringValue(et: EditText): String {
    return getRealValue(et).toString()
}

@BindingAdapter("dateUseSpinners")
fun EditText.setDateUseSpinners(dateUseSpinners: Boolean) {
    setTag(R.id.tag_dateusespinner, dateUseSpinners)
}

