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
import com.ustadmobile.core.util.hoursAndMinsToMillisSinceMidnight
import com.ustadmobile.core.util.millisSinceMidnightToHoursAndMins
import java.util.*

/**
 * Time binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Timepicker
 */
fun updateTimeOnEditText(et: EditText, millisSinceMidnight: Int) {
    if (millisSinceMidnight == 0) {
        et.setText("-")
    }else{
        val dateFormatter = DateFormat.getTimeFormat(et.context)
        val cal = Calendar.getInstance().also {
            val (hour, minute) = millisSinceMidnightToHoursAndMins(millisSinceMidnight)
            it.set(Calendar.HOUR_OF_DAY, hour)
            it.set(Calendar.MINUTE, minute)
        }
        et.setText(dateFormatter.format(Date(cal.timeInMillis)))
    }
}

fun openTimePicker(et: EditText, context: Context, inverseBindingListener: InverseBindingListener) {
    val c = Calendar.getInstance()
    val timeSet = et.getTag(R.id.tag_timelong) as? Int ?: 0
    if(timeSet > 0) {
        val (hour, minute) = millisSinceMidnightToHoursAndMins(timeSet.toInt())
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
    }

    //date listener - opens a new date picker.
    val timeListener = { _: TimePicker, hourOfDay:Int, minute:Int ->
        val millisSinceMidnight = hoursAndMinsToMillisSinceMidnight(hourOfDay, minute)
        et.setTag(R.id.tag_timelong, millisSinceMidnight)
        updateTimeOnEditText(et, millisSinceMidnight)
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
    updateTimeOnEditText(et, time.toInt())
}

@InverseBindingAdapter(attribute = "timeValue")
fun getTimeValue(et: EditText): Long {
    return (et.getTag(R.id.tag_timelong) as? Int ?: 0).toLong()
}
