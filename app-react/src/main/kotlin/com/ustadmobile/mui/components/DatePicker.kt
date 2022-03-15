package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.ext.DATE_FORMAT_DD_MM_YYYY
import com.ustadmobile.util.ext.TIME_FORMAT_H_M
import com.ustadmobile.util.ext.formatDate
import kotlinx.css.Color
import kotlinx.css.RuleSet
import kotlinx.css.color
import mui.lab.*
import mui.material.BaseTextFieldProps
import react.RBuilder
import react.ReactElement
import styled.StyledElementBuilder
import styled.StyledHandler
import styled.StyledProps
import styled.css
import kotlin.js.Date

@JsModule("@mui/lab/AdapterDateFns")
@JsNonModule
private external val AdapterDateFns: dynamic

external interface UMDateTimePickerProps: MobileDatePickerProps, MobileTimePickerProps, StyledProps{
    var renderInput: (BaseTextFieldProps)-> Any?
    var onChange:(dynamic)-> Unit
}

enum class  DateTimePickerOpenTo {
    day, month, year,hours, minutes, seconds
}

private fun RBuilder.renderTextField(
    props: BaseTextFieldProps,
    label: String,
    value: Date? = null,
    format: String? = null,
    error: Boolean = false,
    helperText: String? = null,
    ruleSet: RuleSet ? = StyleManager.defaultFullWidth
): ReactElement {
    return umTextField(
        label = label,
        helperText = helperText,
        error = error){
        attrs.onClick = {
            props.inputProps?.onClick?.invoke(it.asDynamic())
        }

        value?.let {
            attrs.value = it.formatDate(format)
        }
        attrs.variant = props.variant
        ruleSet?.let { css(it) }
    }
}

fun RBuilder.timePicker(
    label: String,
    onChange:(Date)-> Unit,
    value: Date? = null,
    ampm: Boolean = false,
    ampmInClock: Boolean = false,
    inputFormat: String? = null,
    helperText: String? = null,
    error: Boolean = false,
    minTime: Date? = null,
    maxTime: Date? = null,
    onAccept: (()-> Unit)? = null,
    onClose: (()-> Unit)? = null,
    onError: (()-> Unit)? = null,
    okText: String? = null,
    cancelText: String? = null,
    inputVariant: FormControlVariant = FormControlVariant.outlined,
    openTo: DateTimePickerOpenTo? = DateTimePickerOpenTo.minutes,
    toolbarTitle: String? = null,
    views: Array<String>? = null,
    className: String? = null,
    handler: StyledHandler<UMDateTimePickerProps>? = null
) = createStyledComponent(MobileTimePicker, className, handler){
    setProps(this,true,label, onChange, value, inputFormat, helperText,
        error, minTime, maxTime, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views, ampm, ampmInClock)
}

fun RBuilder.datePicker(
    label: String,
    onChange:(Date)-> Unit,
    value: Date? = null,
    inputFormat: String? = null,
    helperText: String? = null,
    error: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    onAccept: (()-> Unit)? = null,
    onClose: (()-> Unit)? = null,
    onError: (()-> Unit)? = null,
    okText: String? = null,
    cancelText: String? = null,
    inputVariant: FormControlVariant = FormControlVariant.outlined,
    openTo: DateTimePickerOpenTo? = DateTimePickerOpenTo.day,
    toolbarTitle: String? = null,
    views: Array<String>? = null,
    className: String? = null,
    handler: StyledHandler<UMDateTimePickerProps>? = null
) = createStyledComponent(MobileDatePicker, className, handler){
    setProps(this,false,label, onChange, value, inputFormat, helperText,
        error, minDate, maxDate, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views)
}

private fun RBuilder.setProps(
    field: StyledElementBuilder<UMDateTimePickerProps>,
    isTime: Boolean,
    label: String,
    onChange:(Date)-> Unit,
    value: Date? = null,
    inputFormat: String? = null,
    helperText: String? = null,
    error: Boolean = false,
    min: Date? = null,
    max: Date? = null,
    onAccept: (()-> Unit)? = null,
    onClose: (()-> Unit)? = null,
    onError: (()-> Unit)? = null,
    cancelText: String? = null,
    okText: String? = null,
    inputVariant: FormControlVariant = FormControlVariant.outlined,
    openTo: DateTimePickerOpenTo? = DateTimePickerOpenTo.day,
    toolbarTitle: String? = null,
    views: Array<String>? = null,
    ampm: Boolean = true,
    ampmInClock: Boolean = false
){
    field.attrs.asDynamic().value = value ?: Date()
    field.attrs.onChange = {
        onChange.invoke(it)
    }
    field.attrs.asDynamic().views  = views
    field.attrs.asDynamic().openTo = openTo.toString()
    field.attrs.renderInput = { it ->
        it.variant = inputVariant.toString()
        renderTextField(it,label,value, inputFormat, error = error,helperText = helperText)
    }
    onError?.let { field.attrs.asDynamic().onError = it}
    onAccept?.let { field.attrs.asDynamic().onAccept = it}
    onClose?.let { field.attrs.asDynamic().onClose = it}
    field.attrs.asDynamic().inputFormat = inputFormat?.lowercase()
    toolbarTitle?.let { field.attrs.asDynamic().toolbarTitle = it}
    field.css {
        color = Color.white
    }

    if(isTime){
        min?.let { field.attrs.asDynamic().minTime = it}
        max?.let { field.attrs.asDynamic().maxTime = it}
        field.attrs.asDynamic().ampm = ampm
        field.attrs.asDynamic().ampmInClock = ampmInClock

    }else {
        min?.let { field.attrs.asDynamic().minDate = it}
        max?.let { field.attrs.asDynamic().maxDate = it}
    }
}

fun RBuilder.umDatePicker(
    label: String,
    onChange:(Date)-> Unit,
    value: Date? = null,
    inputFormat: String? = DATE_FORMAT_DD_MM_YYYY,
    helperText: String? = null,
    error: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    onAccept: (()-> Unit)? = null,
    onClose: (()-> Unit)? = null,
    onError: (()-> Unit)? = null,
    okText: String? = null,
    cancelText: String? = null,
    inputVariant: FormControlVariant = FormControlVariant.outlined,
    openTo: DateTimePickerOpenTo? = DateTimePickerOpenTo.day,
    toolbarTitle: String? = null,
    views: Array<String>? = arrayOf("day" ,"year" , "month"),
    className: String? = null,
    handler: StyledHandler<UMDateTimePickerProps>? = null
) = LocalizationProvider{
    attrs.dateAdapter = AdapterDateFns.default
    datePicker(label, onChange,value, inputFormat, helperText,
        error, minDate, maxDate, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views,className, handler)
}


fun RBuilder.umTimePicker(
    label: String,
    onChange:(Date)-> Unit,
    value: Date? = null,
    inputFormat: String? = TIME_FORMAT_H_M,
    helperText: String? = null,
    error: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    onAccept: (()-> Unit)? = null,
    onClose: (()-> Unit)? = null,
    onError: (()-> Unit)? = null,
    okText: String? = null,
    cancelText: String? = null,
    inputVariant: FormControlVariant = FormControlVariant.outlined,
    openTo: DateTimePickerOpenTo? = DateTimePickerOpenTo.day,
    toolbarTitle: String? = null,
    views: Array<String>? = arrayOf("hours" ,"minutes","seconds"),
    className: String? = null,
    ampm: Boolean = true,
    ampmInClock: Boolean = false,
    handler: StyledHandler<UMDateTimePickerProps>? = null
) = LocalizationProvider{
    attrs.dateAdapter = AdapterDateFns.default
    timePicker(label, onChange, value, ampm, ampmInClock,inputFormat, helperText,
        error, minDate, maxDate, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views,className, handler)
}