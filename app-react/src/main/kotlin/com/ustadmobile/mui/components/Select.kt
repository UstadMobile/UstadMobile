package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Select
import mui.material.SelectProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import react.ReactNode
import styled.StyledHandler

fun RBuilder.umSelect(
    value: Any?,
    open: Boolean? = null,
    error: Boolean? = null,
    disabled: Boolean? = null,
    multiple: Boolean = false,
    variant: FormControlVariant? = null,
    autoWidth: Boolean = false,
    fullWidth: Boolean = false,
    displayEmpty: Boolean = false,
    autoFocus: Boolean? = null,
    id: String? = null,
    labelId: String? = null,
    label: String? = null,
    name: String? = null,
    native: Boolean = false,
    onChange: ((event: Event, child: ReactElement?) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<SelectProps<Any>>? = null
) {
    createStyledComponent(Select, className, handler) {
        autoFocus?.let { attrs.autoFocus = it }
        attrs.autoWidth = autoWidth
        disabled?.let { attrs.disabled = it }
        attrs.displayEmpty = displayEmpty
        error?.let { attrs.error = it }
        attrs.fullWidth = fullWidth
        id?.let { attrs.id = it }
        attrs.multiple = multiple
        attrs.native = native
        attrs.labelId = labelId
        label?.let {
            attrs.label =  ReactNode(it)
        }
        name?.let { attrs.name = it }
        onChange?.let { attrs.onChange = it }
        open?.let { attrs.open = it }
        value?.let { attrs.value = it}
        variant?.let {attrs.variant = it.toString() }
    }
}