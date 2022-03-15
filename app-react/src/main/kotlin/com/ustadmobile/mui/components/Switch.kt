package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.Switch
import mui.material.SwitchProps
import react.RBuilder
import styled.StyledHandler

enum class SwitchSize {
    small, medium
}

fun RBuilder.umSwitch(
    checked: Boolean = false,
    disabled: Boolean = false,
    color: UMColor? = UMColor.secondary,
    size: SwitchSize = SwitchSize.medium,
    className: String? = null,
    handler: StyledHandler<SwitchProps>? = null
) = createStyledComponent(Switch, className, handler) {
    attrs.checked = checked
    attrs.disabled = disabled
    attrs.size = size.toString()
    color?.let {
        attrs.color = it.toString()
    }
}