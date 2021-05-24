package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import react.RBuilder
import react.RComponent
import react.RState
import styled.StyledHandler
import styled.StyledProps
import kotlin.js.Date

@JsModule("@material-ui/pickers/DatePicker")
@JsNonModule
external val datePickerDefault: dynamic

external interface DatePickerProps: StyledProps {
    @JsName("format")
    var format: String?

    @JsName("inputVariant")
    var inputVariant: MFormControlVariant?

    @JsName("value")
    var value: Date

    var onChange:((event: Any) -> Unit)?

}

@Suppress("UnsafeCastFromDynamic")
private val datePicker: RComponent<DatePickerProps, RState> = datePickerDefault.default

fun RBuilder.mDatePicker(
    format: String?,
    value: Date,
    inputVariant: MFormControlVariant? = MFormControlVariant.standard,
    onChange: ((event: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<DatePickerProps>? = null) = createStyled(datePicker) {
    attrs.inputVariant = inputVariant
    attrs.value = value
    attrs.format = format
    attrs.onChange = onChange

    setStyledPropsAndRunHandler(className, handler)
}