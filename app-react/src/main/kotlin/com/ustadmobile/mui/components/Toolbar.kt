package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Toolbar
import mui.material.ToolbarProps
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class ToolbarVariant {
    regular, dense
}

fun RBuilder.umToolbar(
    disableGutters: Boolean = false,
    variant: ToolbarVariant = ToolbarVariant.regular,
    className: String? = null,
    handler: StyledHandler<ToolbarProps>? = null
) = createStyledComponent(Toolbar, className, handler) {
    attrs.disableGutters = disableGutters
    attrs.variant = variant.toString()
}