package com.ustadmobile.port.android.view.binding

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker
import com.toughra.ustadmobile.R
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.systemImpl
import java.text.MessageFormat
import java.util.*


/**
 * Shorthand to check if this Long represents a date that has really been set by the user, or is just
 * a default. 0 and Long.MAX_VALUE are reserved defaults. MAX_VALUE is used for end times to simplify
 * queries.
 */
private val Long.isSet: Boolean
    get() = this != 0L && this != Long.MAX_VALUE

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditText(et: TextView, date: Long) {
    val dateFormatter = DateFormat.getDateFormat(et.context)
    if (!date.isSet) {
        et.setText("")
    } else {
        et.setText(dateFormatter.format(date))
    }
}

/**
 * Data binding Adapter for Date picker types.
 * Contains the logic for linking editText dates with Datepicker
 */

fun updateDateOnEditTextWithExtraText(prepent: String, append: String, et: TextView, date: Long) {
    val dateFormatter = DateFormat.getDateFormat(et.context)
    if (!date.isSet) {
        et.setText(prepent + " " + append)
    } else {
        et.setText(prepent + " " + dateFormatter.format(date) + " - " + append)
    }
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
    val dateDate = Date(date)

    val timeDate = scheduleTimeToDate(time.toInt())

    var text = ""
    text = if (prepend.isEmpty()) {
        dateWithTimeFormat.format(arrayOf(dateDate, timeDate, append))
    } else {
        dateWithTimeFormatWithPrepend.format(arrayOf(prepend, dateDate, timeDate, append))
    }
    if (date == 0L || date == Long.MAX_VALUE) {
        text = ""
    }
    et.text = text

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

@BindingAdapter("dateLong")
fun setDate(et: TextView, date: Long) {
    updateDateOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateLongWithExtra", "dateAppend", "datePrepend")
fun setDateWithExtras(et: TextView, date: Long, append: String?, prepend: String?) {
    val appendString = append ?: ""
    val prependString = prepend ?: ""
    updateDateOnEditTextWithExtraText(prependString, appendString, et, date)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateTimeLongWithExtra", "dateTimeTimeLongWithExtra", "dateTimeAppend", "dateTimePrepend")
fun setDateWithDateExtras(et: TextView, date: Long, time: Long, append: String?, prepend: String?) {
    updateDateTimeOnEditTextWithExtra(prepend ?: "", append ?: "", et, date, time)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("dateTimeLongString")
fun setDateWithDateExtras(et: TextView, date: Long) {
    updateDateTimeOnEditText(et, date)
    et.setTag(R.id.tag_datelong, date)
}

@BindingAdapter("visibleIfDateSet")
fun View.setVisibilityIfSetDate(date: Long){
    visibility = if(date == 0L || date == Long.MAX_VALUE) View.GONE else View.VISIBLE
}

@BindingAdapter("relativeTime")
fun TextView.setDateWithRelativeTime(date: Long){
    text = DateUtils.getRelativeTimeSpanString(date)
}


/**
 * Wrapper to handle when the result of the picker is stored on a string (e.g. CustomFieldValue)
 */
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

@BindingAdapter("dateUseSpinners")
fun TextView.setDateUseSpinners(dateUseSpinners: Boolean) {
    setTag(R.id.tag_dateusespinner, dateUseSpinners)
}

