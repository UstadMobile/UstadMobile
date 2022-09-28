package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.Size
import mui.material.Switch
import mui.material.SwitchColor
import mui.material.SwitchProps
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umSwitch(
    checked: Boolean = false,
    disabled: Boolean = false,
    color: SwitchColor? = SwitchColor.secondary,
    size: Size = Size.medium,
    className: String? = null,
    handler: StyledHandler<SwitchProps>? = null
) = convertFunctionalToClassElement(Switch, className, handler) {
    attrs.checked = checked
    attrs.disabled = disabled
    attrs.size = size.asDynamic()
    color?.let {
        attrs.color = it
    }
}