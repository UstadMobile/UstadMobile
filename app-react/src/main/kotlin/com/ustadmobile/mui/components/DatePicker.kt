package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.ext.DATE_FORMAT_DD_MM_YYYY
import com.ustadmobile.util.ext.TIME_FORMAT_H_M
import com.ustadmobile.util.ext.formatDate
import kotlinx.css.RuleSet
import mui.material.BaseTextFieldProps
import mui.material.FormControlVariant
import muix.pickers.*
import react.RBuilder
import react.ReactElement
import styled.StyledElementBuilder
import styled.StyledHandler
import styled.StyledProps
import styled.css
import kotlin.js.Date

external interface SharedPickerProps: StyledProps{
    var renderInput: (BaseTextFieldProps)-> Any?
    var onChange:(dynamic)-> Unit
}

external interface UMDatePickerProps: MobileDatePickerProps, SharedPickerProps{
    override var components: MobileDatePickerSlotsComponentsProps
}

external interface UMTimePickerProps: MobileTimePickerProps, SharedPickerProps{
    override var components: MobileTimePickerSlotsComponentsProps
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
): ReactElement<*> {
    return umTextField(
        label = label,
        helperText = helperText,
        error = error
    ) {
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
    ampm: Boolean = true,
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
    handler: StyledHandler<UMTimePickerProps>? = null
) = convertFunctionalToClassElement(MobileTimePicker, className, handler){
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
    handler: StyledHandler<UMDatePickerProps>? = null
) = convertFunctionalToClassElement(MobileDatePicker, className, handler){
    setProps(this,false,label, onChange, value, inputFormat, helperText,
        error, minDate, maxDate, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views)
}

private fun RBuilder.setProps(
    builder: StyledElementBuilder<*>,
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
    val component = builder as  StyledElementBuilder<SharedPickerProps>
    component.attrs.asDynamic().value = value ?: Date()
    component.attrs.onChange = {
        onChange.invoke(it)
    }
    component.attrs.asDynamic().views  = views
    component.attrs.asDynamic().openTo = openTo.toString()
    component.attrs.renderInput = {
        it.variant = inputVariant
        renderTextField(it,label,value, inputFormat, error = error,helperText = helperText)
    }
    onError?.let { component.attrs.asDynamic().onError = it}
    onAccept?.let { component.attrs.asDynamic().onAccept = it}
    onClose?.let { component.attrs.asDynamic().onClose = it}
    component.attrs.asDynamic().inputFormat = inputFormat?.lowercase()
    toolbarTitle?.let { component.attrs.asDynamic().toolbarTitle = it}

    if(isTime){
        min?.let { component.attrs.asDynamic().minTime = it}
        max?.let { component.attrs.asDynamic().maxTime = it}
        component.attrs.asDynamic().ampm = ampm
        component.attrs.asDynamic().ampmInClock = ampmInClock

    }else {
        min?.let { component.attrs.asDynamic().minDate = it}
        max?.let { component.attrs.asDynamic().maxDate = it}
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
    handler: StyledHandler<UMDatePickerProps>? = null
) = LocalizationProvider{
    attrs.dateAdapter = AdapterDateFns
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
    ampm: Boolean = false,
    ampmInClock: Boolean = false,
    handler: StyledHandler<UMTimePickerProps>? = null
) = LocalizationProvider {
    attrs.dateAdapter = AdapterDateFns
    timePicker(label, onChange, value, ampm, ampmInClock,inputFormat, helperText,
        error, minDate, maxDate, onAccept, onClose, onError,cancelText, okText,
        inputVariant, openTo,toolbarTitle,views,className, handler)
}