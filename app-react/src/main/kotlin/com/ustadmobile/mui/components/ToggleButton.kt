package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.ToggleButton
import mui.material.ToggleButtonGroup
import mui.material.ToggleButtonGroupProps
import mui.material.ToggleButtonProps
import react.RBuilder
import styled.StyledHandler

enum class ToggleButtonSize{
    small, medium, large
}

enum class ToggleButtonOrientation{
    horizontal, vertical
}

fun RBuilder.umToggleButtonGroup(
    value: Any? = null,
    orientation: ToggleButtonOrientation = ToggleButtonOrientation.horizontal,
    size: ToggleButtonSize = ToggleButtonSize.medium,
    color: UMColor = UMColor.default,
    disabled: Boolean = false,
    className: String? = null,
    onChange: ((Any)->Unit)? = null,
    handler: StyledHandler<ToggleButtonGroupProps>? = null
) = createStyledComponent(ToggleButtonGroup, className, handler) {
    attrs.orientation = orientation.toString()
    attrs.size = size.toString()
    attrs.disabled = disabled
    attrs.value = value
    attrs.onChange = { _, _value ->
        onChange?.invoke(_value)
    }
    attrs.color = color.toString()
}


fun RBuilder.umToggleButton(
    value: Any,
    size: ToggleButtonSize = ToggleButtonSize.medium,
    color: UMColor = UMColor.standard,
    selected: Boolean = false,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<ToggleButtonProps>? = null
) = createStyledComponent(ToggleButton, className, handler) {
    attrs.selected = selected
    attrs.size = size.toString()
    attrs.disabled = disabled
    attrs.value = value
    attrs.color = color.toString()
}