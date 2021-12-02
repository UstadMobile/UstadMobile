package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import mui.material.BaseTextFieldProps
import mui.material.TextField
import mui.material.TextFieldProps
import react.RBuilder
import react.ReactNode
import react.dom.html.InputType
import styled.StyledElementBuilder
import styled.StyledHandler
import styled.StyledProps
import styled.css

@Suppress("EnumEntryName")
enum class TextFieldColor {
    primary, secondary
}

enum class FormControlVariant {
    standard, outlined, filled
}

enum class FormControlMargin {
    none, dense, normal
}

fun RBuilder.umTextField(
    label: String,
    value: String? = null,
    helperText: String? = null,
    defaultValue: String? = null,
    placeholder: String? = null,
    variant: FormControlVariant = FormControlVariant.outlined,
    onChange: ((value: String) -> Unit)? = null,
    type: InputType = InputType.text,
    required: Boolean = false,
    disabled: Boolean = false,
    error: Boolean = false,
    autoFocus: Boolean = false,
    fullWidth: Boolean = false,
    margin: FormControlMargin = FormControlMargin.normal,
    autoComplete: String? = null,
    id: String? = null,
    name: String? = null,
    className: String? = null,
    handler: StyledHandler<UMTextFieldProps>? = null
) = createStyledComponent(TextField, className, handler) {
    css(defaultFullWidth)
    setProps(this, autoComplete, autoFocus, defaultValue, disabled, error, fullWidth, helperText,
        id, label, margin,false, name, onChange,placeholder, required, null, null,
        false, type, value, variant)
}

fun RBuilder.umTextFieldMultiLine(
    label: String,
    value: String? = null,
    helperText: String? = null,
    defaultValue: String? = null,
    placeholder: String? = null,
    variant: FormControlVariant = FormControlVariant.standard,
    onChange: ((value: String) -> Unit)? = null,
    required: Boolean = false,
    disabled: Boolean = false,
    error: Boolean = false,
    autoFocus: Boolean = false,
    fullWidth: Boolean = false,
    margin: FormControlMargin = FormControlMargin.normal,
    rows: Int? = null,
    rowsMax: Int? = null,
    id: String? = null,
    name: String? = null,
    className: String? = null,
    handler: StyledHandler<UMTextFieldProps>? = null
) = createStyledComponent(TextField, className, handler) {
    css(defaultFullWidth)
    setProps(this, null, autoFocus, defaultValue, disabled, error, fullWidth, helperText, id, label, margin,
        true, name, onChange, placeholder, required, rows, rowsMax, false, InputType.text, value, variant)
}

external interface UMTextFieldProps: TextFieldProps, StyledProps, BaseTextFieldProps

fun RBuilder.umTextFieldSelect(
    label: String,
    value: String? = null,
    helperText: String? = null,
    defaultValue: String? = null,
    placeholder: String? = null,
    values: List<Pair<String, String>>? = listOf(),
    variant: FormControlVariant = FormControlVariant.outlined,
    onChange: ((value: String) -> Unit)? = null,
    required: Boolean = false,
    disabled: Boolean = false,
    error: Boolean = false,
    autoFocus: Boolean = false,
    fullWidth: Boolean = false,
    margin: FormControlMargin = FormControlMargin.normal,
    autoComplete: String? = null,
    id: String? = null,
    name: String? = null,
    className: String? = null,
    handler: StyledHandler<UMTextFieldProps>? = null
) = createStyledComponent(TextField, className, handler) {
    setProps(this, autoComplete, autoFocus, defaultValue, disabled, error, fullWidth, helperText, id, label, margin,
        false, name, onChange, placeholder, required, null, null, true, InputType.text, value, variant)
    css{
        +defaultFullWidth
        +alignTextToStart
    }
    if(!values.isNullOrEmpty()){
        values.forEach {
            umMenuItem(it.second, value = it.first){
                css(alignTextToStart)
            }
        }
    }
}


private fun setProps(
    textField: StyledElementBuilder<UMTextFieldProps>,
    autoComplete: String?,
    autoFocus: Boolean,
    defaultValue: String?,
    disabled: Boolean,
    error: Boolean,
    fullWidth: Boolean,
    helperText: String?,
    id: String?,
    label: String,
    margin: FormControlMargin,
    multiline: Boolean,
    name: String?,
    onChange: ((value: String) -> Unit)?,
    placeholder: String?,
    required: Boolean,
    rows: Int?,
    rowsMax: Int?,
    select: Boolean,
    type: InputType,
    value: String?,
    variant: FormControlVariant
) {
    autoComplete?.let { textField.attrs.autoComplete = it }
    textField.attrs.autoFocus = autoFocus
    defaultValue?.let { textField.attrs.defaultValue = it }
    textField.attrs.disabled = disabled
    textField.attrs.error = error
    textField.attrs.fullWidth = fullWidth
    helperText?.let { textField.attrs.helperText = ReactNode(it) }
    id?.let { textField.attrs.id = it }
    textField.attrs.label = ReactNode(label)
    textField.attrs.margin = margin.toString()
    textField.attrs.multiline = multiline
    name?.let { textField.attrs.name = it }
    textField.attrs.onChange = {
        if(!select){
            it.persist()
        }
        onChange?.invoke(it.target.asDynamic().value.toString())
    }
    placeholder?.let { textField.attrs.placeholder = it }
    textField.attrs.required = required
    rows?.let { textField.attrs.rows = it }
    rowsMax?.let { textField.attrs.maxRows = it }
    textField.attrs.select = select
    textField.attrs.type = type
    value?.let { textField.attrs.value = it }
    textField.attrs.variant = variant.toString()
}

