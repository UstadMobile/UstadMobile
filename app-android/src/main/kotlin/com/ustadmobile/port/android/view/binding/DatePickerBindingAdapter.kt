package com.ustadmobile.port.android.view.binding

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditText(et: EditText, date: Long) {
    val dateFormatter: SimpleDateFormat by lazy { SimpleDateFormat("EEE, dd/MMM/yyyy",
            Locale(UstadMobileSystemImpl.instance.getLocale(et.context)?:"")) }
    if (date == 0L) {
        et.setText("-")
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

    val datePicker = DatePickerDialog(
            context, dateListener, c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH))
    datePicker.show()
}


@BindingAdapter("dateLongAttrChanged")
fun getDate(et: EditText, inverseBindingListener: InverseBindingListener){
    et.setOnClickListener {
        openDatePicker2(et, et.context,  inverseBindingListener)
    }
}

@BindingAdapter("dateLong")
fun setDate(et: EditText, date: Long){
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@InverseBindingAdapter(attribute = "dateLong")
fun getRealValue(et: EditText): Long {
    return et.getTag(R.id.tag_datelong) as? Long ?: 0L
}
