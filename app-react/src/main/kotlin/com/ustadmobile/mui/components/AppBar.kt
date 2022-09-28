package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.AppBar
import mui.material.AppBarColor
import mui.material.AppBarPosition
import mui.material.AppBarProps
import react.RBuilder
import styled.StyledHandler


fun RBuilder.umAppBar(
    color: AppBarColor = AppBarColor.primary,
    position: AppBarPosition = AppBarPosition.fixed,
    className: String? = null,
    enableColorOnDark: Boolean = false,
    handler: StyledHandler<AppBarProps>? = null
) = convertFunctionalToClassElement(AppBar, className, handler) {
    attrs.color = color
    attrs.position = position
    attrs.enableColorOnDark = enableColorOnDark
}
