package com.ustadmobile.view.components

import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import kotlinext.js.Object
import react.RBuilder
import react.RComponent
import react.RState
import styled.StyledHandler
import styled.StyledProps
import kotlin.js.Date

@JsModule("@material-ui/pickers")
@JsNonModule
external val datePickerDefault: dynamic

@JsModule("date-fns")
@JsNonModule
external val dateFnsModule: dynamic

@JsModule("@date-io/moment")
@JsNonModule
external val momentDateFnsModule: dynamic

@Suppress("EnumEntryName")
enum class MPickerVariant {
    standard, outlined, filled
}

@Suppress("EnumEntryName")
enum class MPickerOpenTo {
    date, year, month
}

external interface DatePickerProps: StyledProps {
    @JsName("format")
    var format: String?

    @JsName("inputVariant")
    var inputVariant: String?

    @JsName("value")
    var value: Date

    @JsName("onChange")
    var onChange:((event: Any) -> Unit)?

    @JsName("minDate")
    var minDate: Date

    @JsName("maxDate")
    var maxDate: Date

    @JsName("openTo")
    var openTo: String?

}

external interface MuiProviderProps: StyledProps{
    var utils: (Any)-> dynamic
    var children: Any
}

@Suppress("UnsafeCastFromDynamic")
val datePicker: RComponent<DatePickerProps, RState> = datePickerDefault.KeyboardDatePicker

@Suppress("UnsafeCastFromDynamic")
val datePickerProvider: RComponent<MuiProviderProps, RState> = datePickerDefault.MuiPickersUtilsProvider

object DateFunctionUtil {
    val date :(date:dynamic) -> dynamic = { dateFnsModule.getDate(it?:Date())}
    val getDayText :(date:dynamic) -> dynamic = { this.asDynamic().getDaysInMonth(it)}
    val getYearText: (date:dynamic) -> String = { this.asDynamic().getYear(it).toString()}
    val getMothText: (date:dynamic) -> String = { this.asDynamic().getMonth(it).toString()}
    val getDatePickerHeaderText: (date:dynamic) -> dynamic = { this.asDynamic().format(it,"YYY")}
    val getCalendarHeaderText: (date:dynamic) -> dynamic = { this.asDynamic().format(it,"YYY")}
    val getPreviousMonth: (date:dynamic) -> dynamic = { dateFnsModule.subMonths(it,1)}
    val getNextMonth: (date:dynamic) -> dynamic = { dateFnsModule.addMonths(it,1)}
    val getWeekArray: (date:dynamic) -> dynamic = {
        val startDate = dateFnsModule.startOfWeek(dateFnsModule.startOfMonth(it))
        val endDate = dateFnsModule.endOfWeek(dateFnsModule.endOfMonth(it))
        var count = 0
        var current = startDate
        val nestedWeeks = js("[]")
        while (dateFnsModule.isBefore(current,endDate)) {
            val weekNumber = js("Math.floor(count / 7)")
            nestedWeeks[weekNumber] = js("nestedWeeks[weekNumber] || []")
            nestedWeeks[weekNumber].push(current)
            current = dateFnsModule.addDays(current,1)
            count += 1
        }
        nestedWeeks
    }
    val getMonthArray: (date:dynamic) -> dynamic = {
        val firstMonth = dateFnsModule.startOfYear(it)
        val monthArray = js("[firstMonth]")
        while (monthArray.size < 12) {
            val prevMonth = monthArray[monthArray.size - 1]
            monthArray.push(getNextMonth(prevMonth))
        }
        monthArray
    }

    val getYearRange: (start: dynamic, end: dynamic) -> dynamic = { start, end ->
        val startDate = dateFnsModule.startOfYear(start)
        val endDate = dateFnsModule.endOfYear(end)
        val years = js("[]")
        var current = startDate
        while (dateFnsModule.isBefore(current,endDate)) {
            years.push(current)
            current = dateFnsModule.addYears(current,1)
        }
        years
    }
    val create:() -> dynamic = {
        val module = momentDateFnsModule.default
        val obj = js("new module()")
        Object.assign(this, dateFnsModule, obj)
    }
}

fun RBuilder.mDatePickerContainer(format: String = "yyyy-MM-dd", date: Date = Date()) = createStyled(datePickerProvider){
    this.attrs.utils = { DateFunctionUtil.create() }
    this.attrs.children = mDatePicker(format, date, onChange = {})
}

fun RBuilder.mDatePicker(
    format: String?,
    value: Date,
    minDate: Date = dateFnsModule.subYears(value,10),
    maxDate: Date = dateFnsModule.addYears(value,10),
    openTo: MPickerOpenTo? = MPickerOpenTo.date,
    inputVariant: MFormControlVariant? = MFormControlVariant.outlined,
    onChange: ((event: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<DatePickerProps>? = null) = createStyled(datePicker) {
    attrs.inputVariant = inputVariant.toString()
    attrs.value = value
    attrs.format = format
    attrs.openTo = openTo.toString()
    attrs.minDate = minDate
    attrs.maxDate = maxDate
    attrs.onChange = onChange
    setStyledPropsAndRunHandler(className, handler)
}