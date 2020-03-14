package com.ustadmobile.port.android.view.binding

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TimePicker
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import java.text.SimpleDateFormat
import java.util.*

/**
 * Time binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Timepicker
 */

fun updateTimeOnEditText(et: EditText, date: Long) {
    val dateFormatter: SimpleDateFormat by lazy { SimpleDateFormat("HH:mm",
            Locale(UstadMobileSystemImpl.instance.getLocale(et.context)?:"")) }
    if (date == 0L) {
        et.setText("-")
    }else{
        et.setText(dateFormatter.format(date))
    }
}

fun openTimePicker(et: EditText, context: Context, inverseBindingListener: InverseBindingListener) {
    val c = Calendar.getInstance()
    val timeSet = et.getTag(R.id.tag_timelong) as? Long ?: 0L
    if(timeSet > 0) {
        c.timeInMillis = timeSet
    }

    //date listener - opens a new date picker.
    val timeListener = { _: TimePicker, hourOfDay:Int, minute:Int ->
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        et.setTag(R.id.tag_timelong, c.timeInMillis)
        updateTimeOnEditText(et, c.timeInMillis)
        inverseBindingListener.onChange()
    }

    val timePicker = TimePickerDialog(context, timeListener, c.get(Calendar.HOUR),
            c.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
    timePicker.show()
}


@BindingAdapter("timeValueAttrChanged")
fun getTime(et: EditText, inverseBindingListener: InverseBindingListener){
    et.setOnClickListener {
        openTimePicker(et, et.context,  inverseBindingListener)
    }

}

@BindingAdapter("timeValue")
fun setTime(et: EditText, time: Long){
    et.setTag(R.id.tag_timelong, time)
    updateTimeOnEditText(et, time)
}

@InverseBindingAdapter(attribute = "timeValue")
fun getTimeValue(et: EditText): Long {
    return et.getTag(R.id.tag_timelong) as? Long ?: 0L
}
