package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.text.InputFilter
import android.text.Spanned
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.systemImpl
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ReportTemplateListFragment.Companion.REPORT_TITLE_TO_ID
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
@Deprecated("Will be removed once clazzeditpresenter/clazzdetailoverviewpresenter are removed")
fun TextView.setScheduleText(schedule: Schedule) {
}

private fun mkGmtOffsetString(rawOffset: Int): String {
    val gmtOffset =  "${(rawOffset / MS_PER_HOUR)}:" +
            (rawOffset.rem(MS_PER_HOUR) / MS_PER_MIN).toString().padStart(2, '0')
    val plusMinSymbol = if(rawOffset >= 0) "+" else ""
    return "(GMT$plusMinSymbol$gmtOffset)"
}

@SuppressLint("SetTextI18n")
@BindingAdapter("timeZoneText")
fun TextView.setTimeZoneText(timeZone: TimeZone) {
    text = "${mkGmtOffsetString(timeZone.rawOffset)} ${timeZone.id}"
}


@SuppressLint("SetTextI18n")
@BindingAdapter("entityRoleText")
fun TextView.setTimeZoneTextEntity(entityRole: EntityRoleWithNameAndRole?) {

    val scopeText = when (entityRole?.erTableId) {
        Clazz.TABLE_ID -> {
            " (" + context.getString(R.string.clazz) + ")"
        }
        School.TABLE_ID -> {
            " (" + context.getString(R.string.school) + ")"
        }
        Person.TABLE_ID -> {
            " (" + context.getString(R.string.person) + ")"
        }
        else -> {
            ""
        }
    }
    text = entityRole?.entityRoleScopeName?:"" + scopeText
}



@BindingAdapter(value = ["createNewFormatText", "createNewFormatArg"], requireAll = true)
fun TextView.setCreateNewItemText(formatTextId: Int, formatArg: String) {
    text = context.resources.getString(formatTextId, formatArg)
}

@BindingAdapter(value=["textDateRangeFrom", "textDateRangeTo"], requireAll = true)
fun TextView.setTextDateRange(fromDate: Long, toDate: Long) {
    val dateFormatter = DateFormat.getDateFormat(context)
    text = context.getString(R.string.from_to_date, dateFormatter.format(fromDate),
        dateFormatter.format(toDate))
}

@BindingAdapter("runOnClickWhenFocused")
fun TextInputEditText.setRunOnClickWhenFocused(runOnClickWhenFocused: Boolean) {
    if(runOnClickWhenFocused) {
        setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
                v.callOnClick()
        }
    }else {
        setOnFocusChangeListener(null)
    }
}

@BindingAdapter("dontShowZeroInt")
fun TextInputEditText.setValueIfZero(value: Int){
    if(value == 0){
        setText("")
    }else{
        setText(Integer.toString(value))
    }
}

@InverseBindingAdapter(attribute = "dontShowZeroInt")
fun getRealValueInt(et: TextView): Int {
    return et.text.toString().toInt()?:0
}

@BindingAdapter(value = ["minValue", "maxValue"])
fun EditText.setMinMax(min: Int, max: Int){
    filters = arrayOf(InputFilterMinMax(min, max))
}

@SuppressLint("SetTextI18n")
@BindingAdapter("reportTitleText")
fun TextView.setReportTitleText(report: Report) {
    val reportTitleId = report.reportTitleId
    text = REPORT_TITLE_TO_ID[reportTitleId]?.let {
        systemImpl.getString(it, context)} ?: report.reportTitle
}

@SuppressLint("SetTextI18n")
@BindingAdapter("reportDescText")
fun TextView.setReportDescText(report: Report) {
    val reportDescId = report.reportDescId
    text = REPORT_TITLE_TO_ID[reportDescId]?.let {
        systemImpl.getString(it, context)} ?: report.reportDescription
}


class InputFilterMinMax(private val minimumValue: Int, private val maximumValue: Int) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toInt()
            if(input in minimumValue..maximumValue) return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

}