package com.ustadmobile.port.android.view.binding
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.api.PersianPickerDate
import ir.hamsaa.persiandatepicker.api.PersianPickerListener
import ir.hamsaa.persiandatepicker.date.PersianDateImpl
import ir.hamsaa.persiandatepicker.util.PersianCalendarUtils
import org.jetbrains.annotations.NotNull
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


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
        return (
                (this < -43200000 || this > 43200000)  &&
                        (this < (Long.MAX_VALUE - 43200000)  )
                )
    }

@BindingAdapter(value = ["dateTimeInMillis", "timeZoneId", "dateTimeInMillisMode"])
fun TextView.setDateTime2(timeInMillis: Long, timeZoneId: String?, dateTimeInMillisMode: Int) {
    val timeZone = TimeZone.getTimeZone(timeZoneId?:"UTC")
    val formattedTimeInMillis = if (timeInMillis == Long.MAX_VALUE){
        Long.MAX_VALUE - 43200000
    }else{
        timeInMillis
    }
    adapterCalendar.also {
        it.timeZone = timeZone
        it.timeInMillis = formattedTimeInMillis

        if(dateTimeInMillisMode == MODE_START_OF_DAY) {
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }else {
            it.set(Calendar.HOUR_OF_DAY, 23)
            it.set(Calendar.MINUTE, 59)
            it.set(Calendar.SECOND, 59)
            it.set(Calendar.MILLISECOND, 59)
        }
    }
    adapterDateFormat.timeZone = timeZone

    val impl : UstadMobileSystemImpl = (context.applicationContext as DIAware
            ).di.direct.instance()
    val localeString = impl.getLocale(context).toString()
    val persianDate = PersianDateImpl()
    persianDate.setDate(adapterCalendar.timeInMillis)

    text = if(!adapterCalendar.timeInMillis.isSet2){
        ""
    }else{
        if(localeString.startsWith("ps") ||
            localeString.startsWith("fa")
        ){
//            persianDate.persianLongDate
            persianDate.persianYear.toString() + "/" +
                    persianDate.persianMonth + "/" +
                    persianDate.persianDay
        }else {
            adapterDateFormat.format(Date(adapterCalendar.timeInMillis))
        }
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

        val cal = adapterCalendar
        val defaultCal = if(!adapterCalendar.timeInMillis.isSet2){
            Calendar.getInstance()
        }else{
            cal
        }

        val impl : UstadMobileSystemImpl = (context.applicationContext as DIAware
                ).di.direct.instance()
        val localeString = impl.getLocale(context)

        if(localeString.startsWith("ps") || localeString.startsWith("fa")){

            val picker: PersianDatePickerDialog = PersianDatePickerDialog(context)
                .setPositiveButtonString(context.getString(R.string.ok))
                .setNegativeButton(context.getString(R.string.cancel))
                .setTodayButton(context.getString(R.string.today))
                .setTodayButtonVisible(true)
                .setMinYear(1200)
                .setMaxYear(1600)
                .setInitDate(defaultCal.timeInMillis)
                .setActionTextColor(Color.BLACK)
                .setTypeFace(typeface)
                .setTitleType(PersianDatePickerDialog.DAY_MONTH_YEAR)
                .setShowInBottomSheet(true)
                .setListener(object : PersianPickerListener {
                    override fun onDateSelected(@NotNull persianPickerDate: PersianPickerDate) {
                        Toast.makeText(
                            context,
                            persianPickerDate.getPersianYear().toString() + "/" +
                                    persianPickerDate.getPersianMonth() + "/" +
                                    persianPickerDate.getPersianDay() + " timestamp: " + persianPickerDate.timestamp
                                    + " converted : " + UMCalendarUtil.getPrettyDateFromLong(persianPickerDate.timestamp, null) ,
                            Toast.LENGTH_SHORT
                        ).show()

                        adapterCalendar.timeInMillis = persianPickerDate.timestamp

                        val persianDate = PersianDateImpl()
                        persianDate.setDate(adapterCalendar.timeInMillis)

                        text = persianDate.persianLongDate

                        inverseBindingListener.onChange()
                    }

                    override fun onDismissed() {}
                })

            picker.show()

        }else {

            val builder = AlertDialog.Builder(context)
                .setView(dialogView)

            val picker = dialogView.findViewById<DatePicker>(R.id.date_picker)
            picker.init(
                defaultCal.get(Calendar.YEAR), defaultCal.get(Calendar.MONTH), defaultCal.get(Calendar.DAY_OF_MONTH),
                null
            )

            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                cal[Calendar.DAY_OF_MONTH] = picker.dayOfMonth
                cal[Calendar.MONTH] = picker.month
                cal[Calendar.YEAR] = picker.year
                text = adapterDateFormat.format(Date(adapterCalendar.timeInMillis))
                inverseBindingListener.onChange()
            }
            builder.show()
        }
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

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["dateTimeMillisFrom", "dateTimeMillisTo"])
fun TextView.setTextFromToDateTimeMillis(textFromDateLong: Long, textToDateLong: Long) {
    val impl : UstadMobileSystemImpl = (context.applicationContext as DIAware
            ).di.direct.instance()
    val localeString = impl.getLocale(context)
    val persianDateFrom = PersianDateImpl()
    persianDateFrom.setDate(textFromDateLong)

    val persianDateTo = PersianDateImpl()
    persianDateTo.setDate(textToDateLong)
    val dateFormat = android.text.format.DateFormat.getDateFormat(context)

    text = if(localeString.startsWith("ps") || localeString.startsWith("fa")){

        "${if (textFromDateLong > 0) persianDateFrom.persianLongDate else ""} -" +
                " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) persianDateTo.persianLongDate else ""}"

    }else{
        "${if (textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} -" +
                " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) dateFormat.format(textToDateLong) else ""}"
    }


}


//TODO: Update Datepicker to take in timezones

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
