package com.ustadmobile.port.android.view.binding

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.hoursAndMinsToMillisSinceMidnight
import com.ustadmobile.core.util.millisSinceMidnightToHoursAndMins
import com.ustadmobile.port.android.view.ext.calendar
import java.util.*

/**
 * Time binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Timepicker
 */
fun updateTimeOnEditText(et: EditText, millisSinceMidnight: Long) {
    if (millisSinceMidnight == 0L || millisSinceMidnight == Long.MAX_VALUE) {
        et.setText("")
    } else {
        val dateFormatter = DateFormat.getTimeFormat(et.context)
        val cal = Calendar.getInstance().also {
            val (hour, minute) = millisSinceMidnightToHoursAndMins(millisSinceMidnight.toInt())
            it.set(Calendar.HOUR_OF_DAY, hour)
            it.set(Calendar.MINUTE, minute)
        }
        et.setText(dateFormatter.format(Date(cal.timeInMillis)))
    }
}

fun openTimePicker(et: EditText, context: Context, inverseBindingListener: InverseBindingListener) {
    val c = Calendar.getInstance()
    val timeSet = et.getTag(R.id.tag_timelong) as? Int ?: 0
    if (timeSet > 0) {
        val (hour, minute) = millisSinceMidnightToHoursAndMins(timeSet)
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
    }

    //date listener - opens a new date picker.
    val timeListener = { _: TimePicker, hourOfDay: Int, minute: Int ->
        val millisSinceMidnight = hoursAndMinsToMillisSinceMidnight(hourOfDay, minute)
        et.setTag(R.id.tag_timelong, millisSinceMidnight)
        updateTimeOnEditText(et, millisSinceMidnight.toLong())
        inverseBindingListener.onChange()
    }

    val timePicker = TimePickerDialog(context, timeListener, c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
    timePicker.show()
}


fun openTimePickerWithTimeZone(et: EditText, context: Context, inverseBindingListener: InverseBindingListener) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = if(!et.calendar.timeInMillis.isSet){
        calendar.timeInMillis
    }else{
        et.calendar.timeInMillis
    }

    //date listener - opens a new date picker.
    val timeListener = { _: TimePicker, hourOfDay: Int, minute: Int ->
        et.calendar.timeZone = calendar.timeZone
        et.calendar.set(calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                calendar[Calendar.DATE], hourOfDay, minute)
        et.updateTimeWithTimeZone()
        inverseBindingListener.onChange()
    }

    val timePicker = TimePickerDialog(context, timeListener, calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context))
    timePicker.show()
}


@BindingAdapter("timeValueAttrChanged")
fun getTime(et: EditText, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openTimePicker(et, et.context, inverseBindingListener)
    }

}

@BindingAdapter("timeWithZoneValueAttrChanged")
fun getTimeWithZone(et: EditText, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openTimePickerWithTimeZone(et, et.context, inverseBindingListener)
    }
}


@BindingAdapter("timeValue")
fun setTime(et: EditText, time: Long) {
    et.setTag(R.id.tag_timelong, time)
    updateTimeOnEditText(et, time)
}

@BindingAdapter("timeWithZoneValue")
fun EditText.setTimeValueWithZone(time: Long) {
    calendar.timeInMillis = time
    updateTimeWithTimeZone()
}

@BindingAdapter("timeZoneWithTime")
fun EditText.setTimeZoneWithTime(timeZone: String?){
    if(timeZone.isNullOrEmpty()){
        return
    }
    calendar.timeZone = TimeZone.getTimeZone(timeZone)
    updateTimeWithTimeZone()
}

fun EditText.updateTimeWithTimeZone() {
    if (!calendar.timeInMillis.isSet) {
        setText("")
        return
    }

    val dateFormatter = DateFormat.getTimeFormat(context)
    setText(dateFormatter.format(calendar.time))
}


@InverseBindingAdapter(attribute = "timeValue")
fun getTimeValue(et: EditText): Long {
    return (et.getTag(R.id.tag_timelong) as? Int ?: 0).toLong()
}

@InverseBindingAdapter(attribute = "timeWithZoneValue")
fun getTimeWithTimeZoneValue(et: TextView): Long {
    return (et.getTag(R.id.tag_calendar) as? Calendar)?.timeInMillis ?: 0L
}
