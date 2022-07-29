package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.*
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umToggleButtonGroup(
    value: Any? = null,
    orientation: Orientation = Orientation.horizontal,
    size: Size = Size.medium,
    color: ToggleButtonGroupColor = ToggleButtonGroupColor.standard,
    disabled: Boolean = false,
    className: String? = null,
    onChange: ((Any)->Unit)? = null,
    handler: StyledHandler<ToggleButtonGroupProps>? = null
) = convertFunctionalToClassElement(ToggleButtonGroup, className, handler) {
    attrs.orientation = orientation
    attrs.size = size
    attrs.disabled = disabled
    attrs.value = value
    attrs.onChange = { _, _value ->
        onChange?.invoke(_value as Any)
    }
    attrs.color = color
}


fun RBuilder.umToggleButton(
    value: Any,
    size: Size = Size.medium,
    color: ToggleButtonColor = ToggleButtonColor.standard,
    selected: Boolean = false,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<ToggleButtonProps>? = null
) = convertFunctionalToClassElement(ToggleButton, className, handler) {
    attrs.selected = selected
    attrs.size = size
    attrs.disabled = disabled
    attrs.value = value
    attrs.color = color
}