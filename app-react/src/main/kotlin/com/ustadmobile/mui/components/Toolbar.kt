package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.Toolbar
import mui.material.ToolbarProps
import mui.material.ToolbarVariant
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umToolbar(
    disableGutters: Boolean = false,
    variant: ToolbarVariant = ToolbarVariant.regular,
    className: String? = null,
    handler: StyledHandler<ToolbarProps>? = null
) = createReUsableComponent(Toolbar, className, handler) {
    attrs.disableGutters = disableGutters
    attrs.variant = variant
}