package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Schedule
import java.text.MessageFormat
import java.util.*

private val MS_PER_HOUR = 3600000
private val MS_PER_MIN = 60000

private val scheduleMessageFormat: MessageFormat by lazy {
    MessageFormat("{0} - {1} {2,time,short} - {3,time,short}")
}

private fun scheduleTimeToDate(msSinceMidnight: Int) : Date{
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
    cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
    return Date(cal.timeInMillis)
}

@BindingAdapter("scheduleText")
fun TextView.setScheduleText(schedule: Schedule) {
    val systemImpl = UstadMobileSystemImpl.instance
    val frequencyMessageId = ScheduleEditPresenter.FrequencyOption.values()
            .firstOrNull { it.optionVal == schedule.scheduleFrequency }?.messageId ?: 0
    val dayMessageId = ScheduleEditPresenter.DayOptions.values()
            .firstOrNull { it.optionVal == schedule.scheduleDay }?.messageId ?: 0

    text = scheduleMessageFormat.format(arrayOf(systemImpl.getString(frequencyMessageId, context),
            systemImpl.getString(dayMessageId, context),
            scheduleTimeToDate(schedule.sceduleStartTime.toInt()),
            scheduleTimeToDate(schedule.scheduleEndTime.toInt())
    ))
}

@SuppressLint("SetTextI18n")
@BindingAdapter("timeZoneText")
fun TextView.setTimeZoneText(timeZone: TimeZone) {
    val gmtOffset =  "${(timeZone.rawOffset / MS_PER_HOUR)}:" +
            (timeZone.rawOffset.rem(MS_PER_HOUR) / MS_PER_MIN).toString().padStart(2, '0')
    val plusMinSymbol = if(timeZone.rawOffset >= 0) "+" else ""
    text = "(GMT$plusMinSymbol$gmtOffset) ${timeZone.id}"
}

@BindingAdapter(value = ["createNewFormatText", "createNewFormatArg"], requireAll = true)
fun TextView.setCreateNewItemText(formatTextId: Int, formatArg: String) {
    text = context.resources.getString(formatTextId, formatArg)
}
