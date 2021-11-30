package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Icon
import mui.material.IconProps
import react.RBuilder
import react.ReactNode
import styled.StyledHandler


@Suppress("EnumEntryName")
enum class IconColor {
    action, error, disabled, inherit, primary, secondary
}

@Suppress("EnumEntryName")
enum class IconFontSize {
    default, small, large, inherit
}

fun RBuilder.umIcon(
    iconName: String,
    color: IconColor? = IconColor.inherit,
    fontSize: IconFontSize = IconFontSize.default,
    className: String? = null,
    handler: StyledHandler<IconProps>? = null
) = createStyledComponent(Icon, className, handler) {
    color?.let {
        attrs.color = it.toString()
    }
    attrs.fontSize = fontSize.toString()
    childList.add(ReactNode(iconName))
}


