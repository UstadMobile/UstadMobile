package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
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
) = createReUsableComponent(AppBar, className, handler) {
    attrs.color = color
    attrs.position = position
    attrs.enableColorOnDark = enableColorOnDark
}
