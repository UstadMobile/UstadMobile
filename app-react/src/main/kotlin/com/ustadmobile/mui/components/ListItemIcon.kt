package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.ListItemIcon
import mui.material.ListItemIconProps
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umListItemIcon(
    iconName: String? = null,
    className: String? = null,
    handler: StyledHandler<ListItemIconProps>? = null
) = createReUsableComponent(ListItemIcon, className, handler) {
    iconName?.let {
        umIcon(iconName)
    }
}