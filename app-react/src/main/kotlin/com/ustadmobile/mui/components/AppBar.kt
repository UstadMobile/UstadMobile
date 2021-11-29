package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.AppBar
import mui.material.AppBarProps
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class AppBarPosition {
    fixed, absolute, sticky, static, relative
}

enum class AppBarColor {
    primary, secondary, transparent, default, inherit
}

fun RBuilder.umAppBar(
    color: AppBarColor = AppBarColor.primary,
    position: AppBarPosition = AppBarPosition.fixed,
    className: String? = null,
    handler: StyledHandler<AppBarProps>? = null
) = createStyledComponent(AppBar, className, handler) {
    attrs.asDynamic().color = color
    attrs.asDynamic().position = position
}
