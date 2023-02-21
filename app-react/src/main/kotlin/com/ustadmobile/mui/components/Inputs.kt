package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import kotlinx.css.Color
import kotlinx.css.color
import mui.material.Input
import mui.material.InputProps
import dom.html.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import react.dom.events.FormEvent
import react.dom.events.KeyboardEvent
import react.dom.html.InputType
import styled.StyledHandler
import styled.css

fun RBuilder.umInput(
    value: Any? = null,
    required: Boolean? = null,
    disabled: Boolean? = null,
    readOnly: Boolean? = null,
    error: Boolean? = null,
    fullWidth: Boolean = false,
    defaultValue: String? = null,
    placeholder: String? = null,
    disableUnderline: Boolean? = null,
    autoFocus: Boolean? = null,
    type: InputType = InputType.text,
    id: String? = null,
    name: String? = null,
    multiline: Boolean = false,
    rows: Int? = null,
    textColor: Color,
    rowsMax: Int? = null,
    onChange: ((Event) -> Unit)? = null,
    onInput: ((FormEvent<HTMLDivElement>) -> Unit)? = null,
    onKeyDown: ((KeyboardEvent<HTMLElement>) -> Unit)? = null,
    className: String? = null,
    endAdornment: ReactElement<*>? = null,
    handler: StyledHandler<InputProps>? = null
) = convertFunctionalToClassElement(Input, className, handler) {
    autoFocus?.let{ attrs.autoFocus = it }
    defaultValue?.let { attrs.defaultValue = it }
    disabled?.let { attrs.disabled = it }
    disableUnderline?.let { attrs.disableUnderline = it }
    error?.let { attrs.error = it }
    attrs.fullWidth = fullWidth
    id?.let { attrs.id = it }
    attrs.multiline = multiline
    name?.let { attrs.name = it }
    endAdornment?.let {
        attrs.endAdornment = it
    }
    attrs.onChange = {
        it.persist()
        onChange?.invoke(it.nativeEvent)
    }
    placeholder?.let { attrs.placeholder = it }
    readOnly?.let { attrs.readOnly = it }
    required?.let { attrs.required = it }
    rows?.let { attrs.rows = it }
    rowsMax?.let { attrs.maxRows = it }
    attrs.type = type.toString()
    attrs.startAdornment = null
    value?.let { attrs.value = it }
    css {
        color = textColor
    }
    attrs.onKeyDown = {
        onKeyDown?.invoke(it)
    }
    attrs.onInput = {
        onInput?.invoke(it)
    }
}