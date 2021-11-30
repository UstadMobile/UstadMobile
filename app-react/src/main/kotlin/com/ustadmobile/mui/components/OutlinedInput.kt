package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.OutlinedInput
import mui.material.OutlinedInputProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.html.InputType
import styled.StyledHandler

fun RBuilder.umOutlinedInput(
    value: String? = null,
    required: Boolean? = null,
    disabled: Boolean? = null,
    readOnly: Boolean? = null,
    error: Boolean? = null,
    fullWidth: Boolean = false,
    defaultValue: String? = null,
    placeholder: String? = null,
    notched: Boolean? = null,
    autoFocus: Boolean? = null,
    type: InputType = InputType.text,
    id: String? = null,
    name: String? = null,
    multiline: Boolean = false,
    rows: Int? = null,
    rowsMax: Int? = null,
    onChange: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<OutlinedInputProps>? = null
) = createStyledComponent(OutlinedInput, className, handler) {
    autoFocus?.let { attrs.autoFocus = it }
    defaultValue?.let { attrs.defaultValue = it }
    disabled?.let { attrs.disabled = it }
    error?.let { attrs.error = it }
    attrs.fullWidth = fullWidth
    id?.let { attrs.id = it }
    attrs.multiline = multiline
    name?.let { attrs.name = it }
    notched?.let { attrs.notched = it }
    attrs.onChange = {
        onChange?.invoke(it.nativeEvent)
    }
    placeholder?.let { attrs.placeholder = it }
    readOnly?.let { attrs.readOnly = it }
    required?.let { attrs.required = it }
    rows?.let { attrs.rows = it }
    rowsMax?.let { attrs.maxRows = it }
    attrs.type = type.toString()
    value?.let { attrs.value = it }
}