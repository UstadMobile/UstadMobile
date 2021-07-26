package com.ustadmobile.port.android.view.binding
import android.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import java.text.DateFormat
import java.util.*
import java.text.SimpleDateFormat
const val MODE_START_OF_DAY = 1
const val MODE_END_OF_DAY = 2
private val TextView.adapterCalendar: Calendar
    get() {
        return getTag(R.id.tag_dateadapter_calendar)
                as? Calendar ?: Calendar.getInstance().also {
                    setTag(R.id.tag_dateadapter_calendar, it)
                }
    }
private val TextView.adapterDateFormat: DateFormat
    get() {
        return getTag(R.id.tag_dateadapter_dateformatter)
                as? SimpleDateFormat ?: SimpleDateFormat.getDateInstance().also {
                    setTag(R.id.tag_dateadapter_dateformatter, it)
                }
    }

/**
 * Shorthand to check if this Long represents a date that has really been set by the user, or is just
 * a default. 0 and Long.MAX_VALUE are reserved defaults. MAX_VALUE is used for end times to simplify
 * queries.
 */
val Long.isSet2: Boolean
    get(){
        return ((this < -43200000 || this > 43200000)  && this < (Long.MAX_VALUE - 43200000))
    }

@BindingAdapter(value = ["dateTimeInMillis", "timeZoneId", "dateTimeInMillisMode"])
fun TextView.setDateTime2(timeInMillis: Long, timeZoneId: String?, dateTimeInMillisMode: Int) {
    val timeZone = TimeZone.getTimeZone(timeZoneId?:"UTC")
    adapterCalendar.also {
        it.timeZone = timeZone
        it.timeInMillis = timeInMillis

        if(dateTimeInMillisMode == MODE_START_OF_DAY) {
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            //Set seconds, millis etc.
        }else {
            //Set hours, minutes, seconds, millis etc.
        }
    }
    adapterDateFormat.timeZone = timeZone

    text = if(!adapterCalendar.timeInMillis.isSet2){
        ""
    }else{

        adapterDateFormat.format(Date(adapterCalendar.timeInMillis))
    }
}

@InverseBindingAdapter(attribute = "dateTimeInMillis")
fun TextView.getDateTimeInMillis(): Long {
    return adapterCalendar.timeInMillis
}
@BindingAdapter("dateTimeInMillisAttrChanged")
fun TextView.setDateTimeInMillisChanged(inverseBindingListener: InverseBindingListener) {
    setOnClickListener {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker,
            null, false)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val cal = adapterCalendar
        val picker = dialogView.findViewById<DatePicker>(R.id.date_picker)
        picker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            null)
        builder.setPositiveButton(R.string.ok) {dialog, _ ->
            cal[Calendar.DAY_OF_MONTH] = picker.dayOfMonth
            cal[Calendar.MONTH] = picker.month
            cal[Calendar.YEAR] = picker.year
            text = adapterDateFormat.format(Date(adapterCalendar.timeInMillis))
            inverseBindingListener.onChange()
        }
        builder.show()
    }
}

@BindingAdapter("visibleIfDateSet")
fun View.setVisibilityIfSetDate(date: Long){
    visibility = if(date == 0L || date == Long.MAX_VALUE) View.GONE else View.VISIBLE
}


@BindingAdapter("relativeTime")
fun TextView.setDateWithRelativeTime(date: Long){
    text = DateUtils.getRelativeTimeSpanString(date)
}

@BindingAdapter("dateUseSpinners")
fun TextView.setDateUseSpinners(dateUseSpinners: Boolean) {
    setTag(R.id.tag_dateusespinner, dateUseSpinners)
}

@BindingAdapter("timeInMillis")
fun DatePicker.setTimeInMillis(timeInMillis: Long) {
    setTag(R.id.tag_datelong, timeInMillis)
    initIfReady()
}

@BindingAdapter("timeInMillisAttrChanged")
fun DatePicker.setTimeInMillisChangeListener(inverseBindingListener: InverseBindingListener) {
    setTag(R.id.tag_inverse_binding_listener, inverseBindingListener)
    initIfReady()
}

private fun DatePicker.initIfReady() {
    val bindingListener = getTag(R.id.tag_inverse_binding_listener) as? InverseBindingListener
    val timeInMillis = getTag(R.id.tag_datelong) as? Long ?: 0L

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis

    init(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]) {_, _, _, _ ->
        bindingListener?.onChange()
    }
}

@InverseBindingAdapter(attribute = "timeInMillis")
fun DatePicker.getTimeInMillis() : Long{
    return Calendar.getInstance().also {
        it[Calendar.YEAR] = this.year
        it[Calendar.MONTH] = this.month
        it[Calendar.DAY_OF_MONTH] = this.dayOfMonth
    }.timeInMillis
}
