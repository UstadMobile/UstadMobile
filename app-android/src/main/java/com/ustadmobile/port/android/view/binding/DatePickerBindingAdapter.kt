package com.ustadmobile.port.android.view.binding

import android.app.DatePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeFormat
import com.toughra.ustadmobile.R
import java.text.MessageFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditText(et: TextView, date: Long) {
    val dateFormatter = DateFormat.getDateFormat(et.context)
    if (date == 0L) {
        et.setText("")
    }else{
        et.setText(dateFormatter.format(date))
    }
}

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditTextWithExtraText(prepent: String, append: String, et: TextView, date: Long) {
    val dateFormatter = DateFormat.getDateFormat(et.context)
    if (date == 0L) {
        et.setText(prepent + " " + append)
    }else{
        et.setText(prepent + " " + dateFormatter.format(date) + " - " + append)
    }
}

private val MS_PER_HOUR = 3600000
private val MS_PER_MIN = 60000

private fun scheduleTimeToDate(msSinceMidnight: Int) : Date{
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
    cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
    return Date(cal.timeInMillis)
}

private val dateWithTimeFormat: MessageFormat by lazy {
    MessageFormat("{0, date} - {1, time, short} {2}")
}

private val dateWithTimeFormatWithPrepend: MessageFormat by lazy {
    MessageFormat("{0}: {1, date} - {2, time, short} {3}")
}

private val dateTimeOnly: MessageFormat by lazy {
    MessageFormat("{0, date, short} {0, time, short}")
}

fun updateDateTimeOnEditText(et: TextView, date: Long){
    val dateDate = Date(date)
    val text = dateTimeOnly.format(arrayOf(dateDate))
    et.text = text
}
/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateTimeOnEditTextWithExtra(prepend: String, append: String?, et: TextView, date: Long, time: Long) {
    val dateDate = Date(date)

    val timeDate = scheduleTimeToDate(time.toInt())

    var text = ""
    text = if(prepend.isEmpty()) {
        dateWithTimeFormat.format(arrayOf(dateDate, timeDate, append))
    }else{
        dateWithTimeFormatWithPrepend.format(arrayOf(prepend, dateDate, timeDate, append))
    }
    et.text = text

}

fun openDatePicker2(et: TextView, context: Context, inverseBindingListener: InverseBindingListener) {
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
fun getDate(et: TextView, inverseBindingListener: InverseBindingListener){
    et.setOnClickListener {
        openDatePicker2(et, et.context,  inverseBindingListener)
    }
}
@BindingAdapter("dateLongStringAttrChanged")
fun getDateString(et: TextView, inverseBindingListener: InverseBindingListener) {
    et.setOnClickListener {
        openDatePicker2(et, et.context,  inverseBindingListener)
    }
}

@BindingAdapter("dateLong")
fun setDate(et: TextView, date: Long){
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateLongWithExtra", "dateAppend", "datePrepend")
fun setDateWithExtras(et: TextView, date:Long, append: String?, prepend: String?){
    val appendString = append?: ""
    val prependString = prepend?: ""
    updateDateOnEditTextWithExtraText(prependString, appendString, et, date)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateTimeLongWithExtra", "dateTimeTimeLongWithExtra", "dateTimeAppend", "dateTimePrepend")
fun setDateWithDateExtras(et: TextView, date:Long, time:Long, append: String?, prepend: String?){
    updateDateTimeOnEditTextWithExtra(prepend?:"", append?:"", et, date, time)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateTimeLongString")
fun setDateWithDateExtras(et: TextView, date:Long){
    updateDateTimeOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}


/**
 * Wrapper to handle when the result of the picker is stored on a string (e.g. CustomFieldValue)
 */
@BindingAdapter("dateLongString")
fun setDateString(et: TextView, dateLongString: String?){
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

@BindingAdapter("dateUseSpinners")
fun TextView.setDateUseSpinners(dateUseSpinners: Boolean) {
    setTag(R.id.tag_dateusespinner, dateUseSpinners)
}

