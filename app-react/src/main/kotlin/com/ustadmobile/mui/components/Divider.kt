package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.Divider
import mui.material.DividerProps
import mui.material.DividerVariant
import mui.material.Orientation
import react.RBuilder
import styled.StyledHandler
import styled.StyledProps


external interface UMDividerProps : DividerProps, StyledProps

fun RBuilder.umDivider(
    variant: DividerVariant = DividerVariant.fullWidth,
    light: Boolean = false,
    absolute: Boolean = false,
    orientation: Orientation = Orientation.horizontal,
    component: String = "hr",
    className: String? = null,
    handler: StyledHandler<UMDividerProps>? = null
) = createReUsableComponent(Divider, className, handler) {
    attrs.absolute = absolute
    attrs.asDynamic().component = component
    attrs.light = light
    attrs.orientation = orientation
    attrs.variant = variant
}