package com.ustadmobile.port.android.view.binding

import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.systemImpl
import com.ustadmobile.port.android.view.ext.calendar
import java.text.MessageFormat
import java.util.*


/**
 * Shorthand to check if this Long represents a date that has really been set by the user, or is just
 * a default. 0 and Long.MAX_VALUE are reserved defaults. MAX_VALUE is used for end times to simplify
 * queries.
 */
val Long.isSet: Boolean
    get() = this != 0L && this != Long.MAX_VALUE

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditText(et: TextView, date: Long, timeZone: String? = null) {
    if(!date.isSet){
        et.text = ""
        return
    }
    val dateFormatter = DateFormat.getDateFormat(et.context)
    dateFormatter.takeIf { timeZone != null }?.timeZone = TimeZone.getTimeZone(timeZone)
    et.text = dateFormatter.format(date)
}

private fun EditText.updateDateWithTimeZone(){
    if(!calendar.timeInMillis.isSet){
        setText("")
        return
    }
    val dateFormatter = DateFormat.getDateFormat(context)
    setText(dateFormatter.format(calendar.time))
}

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditTextWithExtraText(prepent: String, append: String, et: TextView, date: Long) {
    if (!date.isSet) {
        et.text = "$prepent $append"
        return
    }
    val dateFormatter = DateFormat.getDateFormat(et.context)
    et.text = "$prepent ${dateFormatter.format(date)} $append"
}

private val MS_PER_HOUR = 3600000
private val MS_PER_MIN = 60000

private fun scheduleTimeToDate(msSinceMidnight: Int): Date {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
    cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
    return Date(cal.timeInMillis)
}

val dateWithTimeFormat: MessageFormat by lazy {
    MessageFormat("{0, date} - {1, time, short} {2}")
}

val dateWithTimeFormatWithPrepend: MessageFormat by lazy {
    MessageFormat("{0}: {1, date} - {2, time, short} {3}")
}

private val dateTimeOnly: MessageFormat by lazy {
    MessageFormat("{0, date, short} {0, time, short}")
}

fun updateDateTimeOnEditText(et: TextView, date: Long) {
    val dateDate = Date(date)
    val text = dateTimeOnly.format(arrayOf(dateDate))
    et.text = text
}

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateTimeOnEditTextWithExtra(prepend: String, append: String?, et: TextView, date: Long, time: Long) {
    if (!date.isSet) {
        et.text = ""
        return
    }
    val dateDate = Date(date)
    val timeDate = scheduleTimeToDate(time.toInt())

    et.text = if (prepend.isEmpty()) {
        dateWithTimeFormat.format(arrayOf(dateDate, timeDate, append))
    } else {
        dateWithTimeFormatWithPrepend.format(arrayOf(prepend, dateDate, timeDate, append))
    }
}

fun openDateTimeZonePicker(et: EditText, context: Context, inverseBindingListener: InverseBindingListener){
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = if(!et.calendar.timeInMillis.isSet){
        calendar.timeInMillis
    }else{
        et.calendar.timeInMillis
    }
    val builder = AlertDialog.Builder(context)

    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker,
            null, false)

    builder.setView(dialogView)

    val picker = dialogView.findViewById<DatePicker>(R.id.date_picker)
    picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH), null)

    builder.setPositiveButton(et.systemImpl.getString(MessageID.ok,
            context)) { dialog, _ ->

        et.calendar.timeZone = calendar.timeZone
        et.calendar.set(picker.year, picker.month, picker.dayOfMonth,
                calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
        et.setTag(R.id.tag_calendar, et.calendar)
        et.updateDateWithTimeZone()
        inverseBindingListener.onChange()
    }
    builder.setNegativeButton(et.systemImpl.getString(MessageID.cancel,
            context)) { dialog, _ -> dialog.dismiss() }
    builder.show()
}

fun openDatePicker2(et: TextView, context: Context, inverseBindingListener: InverseBindingListener) {
    val c = Calendar.getInstance()
    val currentDate = et.getTag(R.id.tag_datelong) as? Long ?: 0L
    c.timeInMillis = if(!currentDate.isSet){
        c.time.time
    }else{
        currentDate
    }

    val builder = AlertDialog.Builder(context)

    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker,
            null, false)

    builder.setView(dialogView)

    val picker = dialogView.findViewById<DatePicker>(R.id.date_picker)
    picker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null)

    builder.setPositiveButton(et.systemImpl.getString(MessageID.ok,
            context)) { dialog, _ ->

        c[Calendar.DAY_OF_MONTH] = picker.dayOfMonth
        c[Calendar.MONTH] = picker.month
        c[Calendar.YEAR] = picker.year

        et.setTag(R.id.tag_datelong, c.timeInMillis)
        updateDateOnEditText(et, c.timeInMillis)
        inverseBindingListener.onChange()
    }
    builder.setNegativeButton(et.systemImpl.getString(MessageID.cancel,
            context)) { dialog, _ -> dialog.dismiss() }
    builder.show()
}


@BindingAdapter("dateLongAttrChanged")
fun getDate(et: TextView, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openDatePicker2(et, et.context, inverseBindingListener)
    }
}

@BindingAdapter("dateLongStringAttrChanged")
fun getDateString(et: TextView, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openDatePicker2(et, et.context, inverseBindingListener)
    }
}

@BindingAdapter("dateTimeLongAttrChanged")
fun EditText.getDateTimeLong(inverseBindingListener: InverseBindingListener){
    setOnClickListener{
        openDateTimeZonePicker(this, context, inverseBindingListener)
    }
}

@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateLong")
fun setDate(et: TextView, date: Long) {
    updateDateOnEditText(et, date, null)
    et.setTag(R.id.tag_datelong, date)
}

@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateTimeLong")
fun EditText.setDateTime(date: Long){
    calendar.timeInMillis = date
    updateDateWithTimeZone()
}

@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("timeZoneWithDate")
fun EditText.setTimeZoneWithDate(timeZone: String?){
    if(timeZone.isNullOrEmpty()){
        return
    }
    calendar.timeZone = TimeZone.getTimeZone(timeZone)
    updateDateWithTimeZone()
}



@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateLongWithExtra", "dateAppend", "datePrepend")
fun setDateWithExtras(et: TextView, date: Long, append: String?, prepend: String?) {
    val appendString = append ?: ""
    val prependString = prepend ?: ""
    updateDateOnEditTextWithExtraText(prependString, appendString, et, date)
    et.setTag(R.id.tag_datelong, date)
}

@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateTimeLongWithExtra", "dateTimeTimeLongWithExtra", "dateTimeAppend", "dateTimePrepend")
fun setDateWithDateExtras(et: TextView, date: Long, time: Long, append: String?, prepend: String?) {
    updateDateTimeOnEditTextWithExtra(prepend ?: "", append ?: "", et, date, time)
    et.setTag(R.id.tag_datelong, date)
}

@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateTimeLongString")
fun setDateWithDateExtras(et: TextView, date: Long) {
    updateDateTimeOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}


/**
 * Wrapper to handle when the result of the picker is stored on a string (e.g. CustomFieldValue)
 */
@Deprecated("Use datePickerBindingAdapter2")
@BindingAdapter("dateLongString")
fun setDateString(et: TextView, dateLongString: String?) {
    val date = dateLongString?.toLong() ?: 0L
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@InverseBindingAdapter(attribute = "dateLong")
fun getRealValue(et: TextView): Long {
    return et.getTag(R.id.tag_datelong) as? Long ?: 0L
}

@InverseBindingAdapter(attribute = "dateLongString")
fun getRealStringValue(et: TextView): String {
    return getRealValue(et).toString()
}

@InverseBindingAdapter(attribute = "dateTimeLong")
fun getRealDateTimeZoneValue(et: TextView): Long {
    return (et.getTag(R.id.tag_calendar) as? Calendar)?.timeInMillis ?: 0L
}

//TODO: Move DatePicker to use timezones

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




