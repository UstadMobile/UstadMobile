package com.ustadmobile.port.android.view.binding
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.fragment.app.findFragment
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.min
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import com.aminography.primecalendar.common.CalendarFactory
import com.aminography.primecalendar.common.CalendarType
import com.aminography.primedatepicker.common.PickType
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.SingleDayPickCallback
import com.aminography.primecalendar.persian.PersianCalendar
import com.ustadmobile.port.android.view.UstadBaseFragment


const val MODE_START_OF_DAY = 1
const val MODE_END_OF_DAY = 2

const val TWELVE_HOURS_IN_MS = 43200000

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
                (this < -TWELVE_HOURS_IN_MS || this > TWELVE_HOURS_IN_MS)  &&
                (this < (Long.MAX_VALUE - TWELVE_HOURS_IN_MS)  )
                )
    }

@BindingAdapter(value = ["dateTimeInMillis", "timeZoneId", "dateTimeInMillisMode"])
fun TextView.setDateTime2(timeInMillis: Long, timeZoneId: String?, dateTimeInMillisMode: Int) {
    val timeZone = TimeZone.getTimeZone(timeZoneId?:"UTC")

    adapterCalendar.also {
        it.timeZone = timeZone

        //If Long.MAX_VALUE is used, it gets wrapped (so avoid using anything within 12 hours of Long.MAX)
        it.timeInMillis = min(timeInMillis, Long.MAX_VALUE - TWELVE_HOURS_IN_MS)

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
    val localeString = impl.getDisplayedLocale(context)
    text = when {
        !adapterCalendar.timeInMillis.isSet2 -> ""
        (localeString.startsWith("fa") || localeString.startsWith("ps")) -> {

            val calendarType = CalendarType.PERSIAN
            val locale = Locale(localeString)
            val pickType = PickType.SINGLE
            val minDateCalendar = null
            val maxDateCalendar = null
            val today = CalendarFactory.newInstance(calendarType, locale)
            val timeZone =adapterDateFormat.timeZone


            var persianCalendar = PersianCalendar(timeZone, locale).also{
                it.timeInMillis = adapterCalendar.timeInMillis}

            persianCalendar.get(Calendar.YEAR).toString() + "/" +
                    (persianCalendar.get(Calendar.MONTH) + 1).toString() + "/" +
                    persianCalendar.get(Calendar.DAY_OF_MONTH).toString()


        }
        else -> adapterDateFormat.format(Date(adapterCalendar.timeInMillis))
    }
}

@InverseBindingAdapter(attribute = "dateTimeInMillis")
fun TextView.getDateTimeInMillis(): Long {
    return adapterCalendar.timeInMillis
}

const val PICKER_TAG = "PrimeDatePickerBottomSheet"

@BindingAdapter("dateTimeInMillisAttrChanged")
fun TextView.setDateTimeInMillisChanged(inverseBindingListener: InverseBindingListener) {
    setOnClickListener {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker,
            null, false)

        val cal = adapterCalendar

        val impl : UstadMobileSystemImpl = (context.applicationContext as DIAware
                                            ).di.direct.instance()
        val localeString = impl.getDisplayedLocale(context)

        if(localeString.startsWith("ps") || localeString.startsWith("fa")){

            val calendarType = CalendarType.PERSIAN
            val locale = Locale(localeString)

            val timeOffset = Long.MAX_VALUE - (1000*60*60*26)
            val calTimeInMillis = cal.timeInMillis
            val today = CalendarFactory.newInstance(calendarType, locale)

            if(calTimeInMillis < timeOffset && calTimeInMillis > 0){
                today.timeInMillis = calTimeInMillis
            }

            var datePicker: PrimeDatePicker?

            datePicker = PrimeDatePicker.Companion.dialogWith(today)
                .pickSingleDay()
                .initiallyPickedSingleDay(today)
                .build()

            datePicker.setDayPickCallback(
                SingleDayPickCallback { singleDay ->
                    val dateText = singleDay.get(Calendar.YEAR).toString() + "/" +
                            (singleDay.get(Calendar.MONTH) + 1).toString() + "/" +
                            singleDay.get(Calendar.DAY_OF_MONTH).toString()
                    text = dateText
                    adapterCalendar.timeInMillis = singleDay.timeInMillis
                    inverseBindingListener.onChange()
                }
            )


            datePicker.show(
                findFragment<UstadBaseFragment>().childFragmentManager, PICKER_TAG
            )

            datePicker.setOnDismissListener { datePicker = null }

        }else {

            val builder = AlertDialog.Builder(context)
                .setView(dialogView)

            val picker = dialogView.findViewById<DatePicker>(R.id.date_picker)
            picker.takeIf { cal.timeInMillis.isSet2 }?.init(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
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
    val localeString = impl.getDisplayedLocale(context)

    val dateFormat = android.text.format.DateFormat.getDateFormat(context)

    text = if(localeString.startsWith("ps") || localeString.startsWith("fa")){


        val calendarType = CalendarType.PERSIAN
        val locale = Locale(localeString)
        val today = CalendarFactory.newInstance(calendarType, locale)
        val timeZone =adapterDateFormat.timeZone


        var persianFromCalendar = PersianCalendar(timeZone, locale).also{
            it.timeInMillis = textFromDateLong}

        var persianToCalendar = PersianCalendar(timeZone, locale).also{
            it.timeInMillis = textToDateLong}

        val persianFromText = persianFromCalendar.get(Calendar.YEAR).toString() + "/" +
                (persianFromCalendar.get(Calendar.MONTH) + 1).toString() + "/" +
                persianFromCalendar.get(Calendar.DAY_OF_MONTH).toString()

        val persianToText = persianToCalendar.get(Calendar.YEAR).toString() + "/" +
                (persianToCalendar.get(Calendar.MONTH) + 1).toString() + "/" +
                persianToCalendar.get(Calendar.DAY_OF_MONTH).toString()

        "${if (textFromDateLong > 0) persianFromText else ""} -" +
                " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) 
                    persianToText else ""}"

    }else{
        "${if (textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} -" +
                " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) dateFormat.format(textToDateLong) else ""}"
    }


}

