package com.ustadmobile.view.components

import com.ccfraser.muirwik.components.MTextFieldProps
import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.variant
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import kotlinx.css.RuleSet
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement
import styled.StyledElementBuilder
import styled.StyledHandler
import styled.StyledProps
import styled.css
import kotlin.js.Date

@JsModule("@material-ui/pickers")
@JsNonModule
private external val uiPicker: dynamic


@JsModule("@date-io/moment")
@JsNonModule
private external val ioMoment: dynamic

enum class MDateTimePickerOpenTo{
    date, year, month, hours, minutes, seconds
}


enum class MDateTimePickerOrientation{
    portrait, landscape
}

enum class MDateTimePickerVariant{
    dialog,inline, static
}

enum class MDateTimePickerType{
    datetime, date ,time
}

external interface MDateTimePickerProps: StyledProps{
    var value: Date
    var onChange:(dynamic)-> Unit
    var autoOk: Boolean
    var disabled: Boolean
    var emptyLabel:String
    var format: String
    var disableToolbar: Boolean
    var initialFocusedDate: Date
    var inputVariant: String
    var orientation: String
    var views: Array<String>?
    var variant: String
    var TextFieldComponent: ((MTextFieldProps)-> Any?)?
    var openTo: MDateTimePickerOpenTo?
    var allowKeyboardControl: Boolean
    var animateYearScrolling: Boolean
    var disableFuture: Boolean
    var disablePast: Boolean
    var minDate: Date
    var maxDate: Date
    var ampm: Boolean
}

external interface MuiPickersUtilsProvider: StyledProps{
    var utils: ()-> Any
}

@Suppress("UnsafeCastFromDynamic")
private val uiDateTimePickerComponent: RComponent<MDateTimePickerProps, RState> = uiPicker.DateTimePicker

@Suppress("UnsafeCastFromDynamic")
private val uiDatePickerComponent: RComponent<MDateTimePickerProps, RState> = uiPicker.DatePicker

@Suppress("UnsafeCastFromDynamic")
private val uiTimePickerComponent: RComponent<MDateTimePickerProps, RState> = uiPicker.TimePicker

@Suppress("UnsafeCastFromDynamic")
private val uiProviderComponent: RComponent<MuiPickersUtilsProvider, RState> = uiPicker.MuiPickersUtilsProvider

private fun setAttributes(
    field: StyledElementBuilder<MDateTimePickerProps>,
    value: Date = Date(),
    onChange:(Any)-> Unit,
    autoOk: Boolean = false,
    disabled: Boolean = false,
    emptyLabel:String? = null,
    format: String? = null,
    disableToolbar: Boolean = false,
    initialFocusedDate: Date? = null,
    inputVariant: MFormControlVariant? = null,
    orientation: MDateTimePickerOrientation = MDateTimePickerOrientation.portrait,
    views: Array<String>? = null,
    variant: MDateTimePickerVariant = MDateTimePickerVariant.dialog,
    textFieldComponent: ((MTextFieldProps)-> Any?)? = null,
    allowKeyboardControl: Boolean = true,
    animateYearScrolling: Boolean = false,
    openTo: MDateTimePickerOpenTo? = null,
    disableFuture: Boolean = false,
    disablePast: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    ampm: Boolean = true
){
    field.attrs.value = value
    field.attrs.onChange = onChange
    field.attrs.autoOk = autoOk
    field.attrs.disabled = disabled
    field.attrs.disableToolbar = disableToolbar
    field.attrs.views  = views
    field.attrs.allowKeyboardControl = allowKeyboardControl
    field.attrs.animateYearScrolling = animateYearScrolling
    field.attrs.disableFuture = disableFuture
    field.attrs.disablePast = disablePast
    field.attrs.ampm = ampm
    format?.let { field.attrs.format = it}
    minDate?.let { field.attrs.minDate = it}
    maxDate?.let { field.attrs.maxDate = it}
    field.attrs.emptyLabel = emptyLabel ?: ""
    variant.let { field.attrs.variant  = it.toString() }
    initialFocusedDate?.let{ field.attrs.initialFocusedDate = it}
    inputVariant?.let{ field.attrs.inputVariant = it.toString()}
    orientation.let { field.attrs.orientation = it.toString() }
    textFieldComponent?.let { field.attrs.TextFieldComponent = it }
    field.attrs.openTo = openTo
}


private fun RBuilder.mUiTimePickerProvider(
    className: String? = null,
    handler: StyledHandler<MuiPickersUtilsProvider>) = createStyled(uiProviderComponent){
    attrs.className = className
    attrs.utils = ioMoment.default
    setStyledPropsAndRunHandler(className, handler)
}


private fun RBuilder.uiDateTimePicker(
    value: Date = Date(),
    onChange:(dynamic)-> Unit,
    autoOk: Boolean = false,
    disabled: Boolean = false,
    emptyLabel:String? = null,
    format: String? = null,
    disableToolbar: Boolean = false,
    initialFocusedDate: Date? = null,
    inputVariant: MFormControlVariant? = null,
    orientation: MDateTimePickerOrientation = MDateTimePickerOrientation.portrait,
    views: Array<String>? = null,
    variant: MDateTimePickerVariant = MDateTimePickerVariant.dialog,
    textFieldComponent: ((MTextFieldProps)-> Any?)? = null,
    allowKeyboardControl: Boolean = true,
    animateYearScrolling: Boolean = false,
    openTo: MDateTimePickerOpenTo? = null,
    disableFuture: Boolean = false,
    disablePast: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    ampm: Boolean = true,
    className: String? = null,
    handler: StyledHandler<MDateTimePickerProps>) = createStyled(uiDateTimePickerComponent){

    setAttributes(this,value,onChange,autoOk,disabled,emptyLabel,
        format, disableToolbar,initialFocusedDate,inputVariant,orientation,
        views ?: arrayOf("date" ,"year" , "month" , "hours" , "minutes"),
        variant,textFieldComponent, allowKeyboardControl,animateYearScrolling,
        openTo ?: MDateTimePickerOpenTo.month, disableFuture,disablePast,minDate, maxDate,ampm)

    setStyledPropsAndRunHandler(className, handler)
}

private fun RBuilder.uiDatePicker(
    value: Date = Date(),
    onChange:(dynamic)-> Unit,
    autoOk: Boolean = false,
    disabled: Boolean = false,
    emptyLabel:String? = null,
    format: String? = null,
    disableToolbar: Boolean = false,
    initialFocusedDate: Date? = null,
    inputVariant: MFormControlVariant? = null,
    orientation: MDateTimePickerOrientation = MDateTimePickerOrientation.portrait,
    views: Array<String>? = null,
    variant: MDateTimePickerVariant = MDateTimePickerVariant.dialog,
    textFieldComponent: ((MTextFieldProps)-> Any?)? = null,
    allowKeyboardControl: Boolean = true,
    animateYearScrolling: Boolean = false,
    openTo: MDateTimePickerOpenTo? = null,
    disableFuture: Boolean = false,
    disablePast: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    className: String? = null,
    handler: StyledHandler<MDateTimePickerProps>) = createStyled(uiDatePickerComponent){

    setAttributes(this, value,onChange,autoOk,disabled,emptyLabel,
        format ?: "DD/MM/YYYY", disableToolbar,initialFocusedDate,inputVariant,orientation,
        views ?: arrayOf("date" ,"year" , "month"),
        variant,textFieldComponent, allowKeyboardControl,animateYearScrolling,
        openTo ?: MDateTimePickerOpenTo.month, disableFuture,disablePast,minDate, maxDate)

    setStyledPropsAndRunHandler(className, handler)
}

private fun RBuilder.uiTimePicker(
    value: Date = Date(),
    onChange:(dynamic)-> Unit,
    autoOk: Boolean = false,
    disabled: Boolean = false,
    emptyLabel:String? = null,
    format: String? = null,
    disableToolbar: Boolean = false,
    initialFocusedDate: Date? = null,
    inputVariant: MFormControlVariant? = null,
    orientation: MDateTimePickerOrientation = MDateTimePickerOrientation.portrait,
    views: Array<String>? = null,
    variant: MDateTimePickerVariant = MDateTimePickerVariant.dialog,
    textFieldComponent: ((MTextFieldProps)-> Any?)? = null,
    ampm: Boolean = true,
    openTo: MDateTimePickerOpenTo? = null,
    className: String? = null,
    handler: StyledHandler<MDateTimePickerProps>) = createStyled(uiTimePickerComponent){

    setAttributes(this,  value,onChange,autoOk,disabled,emptyLabel,
        format ?:"HH:mm", disableToolbar,initialFocusedDate,inputVariant,orientation,
        views ?: arrayOf("hours" , "minutes"),
        variant, textFieldComponent,ampm, openTo = openTo ?: MDateTimePickerOpenTo.hours)

    setStyledPropsAndRunHandler(className, handler)
}


private fun RBuilder.renderTextField(
    props: MTextFieldProps,
    label: String,
    error: Boolean = false,
    helperText: String? = null,
    ruleSet: RuleSet ? = null): ReactElement {
    return mTextField(label,props.value,
        helperText = helperText,
        error = error,
        onChange = props.onChange){
        attrs.onClick = props.onClick
        attrs.variant = props.variant
        ruleSet?.let { css(it) }
    }
}

fun RBuilder.mDateTimePicker(
    label: String,
    error: Boolean = false,
    value: Date = Date(),
    helperText: String? = null,
    onChange:(Long, Boolean)-> Unit,
    autoOk: Boolean = false,
    disabled: Boolean = false,
    emptyLabel:String? = null,
    format: String? = null,
    disableToolbar: Boolean = false,
    initialFocusedDate: Date? = value,
    inputVariant: MFormControlVariant? = MFormControlVariant.outlined,
    orientation: MDateTimePickerOrientation = MDateTimePickerOrientation.portrait,
    views: Array<String> ? = null,
    variant: MDateTimePickerVariant = MDateTimePickerVariant.dialog,
    allowKeyboardControl: Boolean = true,
    animateYearScrolling: Boolean = false,
    openTo: MDateTimePickerOpenTo? = null,
    disableFuture: Boolean = false,
    disablePast: Boolean = false,
    minDate: Date? = null,
    maxDate: Date? = null,
    ampm: Boolean = true,
    pickerType: MDateTimePickerType = MDateTimePickerType.datetime,
    className: String? = null,
    ruleSet: RuleSet ? = null){

    mUiTimePickerProvider(className = className) {

        if(pickerType == MDateTimePickerType.datetime)
            uiDateTimePicker(value, { onChange("${it.valueOf()}".toLong(), it.isUTC()) },
                autoOk,disabled,emptyLabel, format, disableToolbar,initialFocusedDate,
                inputVariant,orientation, views,variant,
                {renderTextField(it, label, error, helperText,ruleSet)}, allowKeyboardControl,
                animateYearScrolling, MDateTimePickerOpenTo.month, disableFuture,disablePast,
                minDate, maxDate,ampm,className){

            }

        if(pickerType == MDateTimePickerType.time)
            uiTimePicker(
                value, { onChange("${it.valueOf()}".toLong(), it.isUTC()) },autoOk,disabled,emptyLabel,
                format, disableToolbar,initialFocusedDate,inputVariant,orientation,
                views,variant, {renderTextField(it, label, error,helperText, ruleSet)},ampm, openTo, className){

            }

        if(pickerType == MDateTimePickerType.date)
            uiDatePicker(value, { onChange("${it.valueOf()}".toLong(), it.isUTC()) },autoOk,disabled,emptyLabel,
                format, disableToolbar,initialFocusedDate,inputVariant,orientation,
                views,variant,{renderTextField(it, label, error,helperText, ruleSet)}, allowKeyboardControl,animateYearScrolling,
                openTo, disableFuture,disablePast,minDate, maxDate,className){
            }
    }
}
