package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Divider
import mui.material.DividerProps
import react.RBuilder
import styled.StyledHandler
import styled.StyledProps

@Suppress("EnumEntryName")
enum class MDividerOrientation {
    horizontal, vertical
}

@Suppress("EnumEntryName")
enum class MDividerVariant {
    fullWidth, inset, middle
}

external interface UMDividerProps : DividerProps, StyledProps

fun RBuilder.umDivider(
    variant: MDividerVariant = MDividerVariant.fullWidth,
    light: Boolean = false,
    absolute: Boolean = false,
    orientation: MDividerOrientation = MDividerOrientation.horizontal,
    component: String = "hr",
    className: String? = null,
    handler: StyledHandler<UMDividerProps>? = null
) = createStyledComponent(Divider, className, handler) {
    attrs.absolute = absolute
    attrs.asDynamic().component = component
    attrs.light = light
    attrs.orientation = orientation.toString()
    attrs.variant = variant.toString()
}