package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.Icon
import mui.material.IconColor
import mui.material.IconProps
import mui.material.IconSize
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

fun RBuilder.umIcon(
    iconName: String,
    color: IconColor = IconColor.inherit,
    size: IconSize = IconSize.medium,
    className: String? = null,
    handler: StyledHandler<IconProps>? = null
) = convertFunctionalToClassElement(Icon, className, handler) {
    childList.add(ReactNode(iconName))
    attrs.color = color
    attrs.fontSize = size
}


