package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Paper
import mui.material.PaperProps
import react.RBuilder
import styled.StyledHandler

enum class PaperVariant {
    elevation, outlined
}

fun RBuilder.umPaper(
    component: String = "div",
    elevation: Int = 2,
    square: Boolean = false,
    variant: PaperVariant = PaperVariant.elevation,
    className: String? = null,
    handler: StyledHandler<PaperProps>? = null
) = createStyledComponent(Paper, className, handler) {
    attrs.asDynamic().component = component
    attrs.elevation = elevation
    attrs.square = square
    attrs.asDynamic().variant = variant
}