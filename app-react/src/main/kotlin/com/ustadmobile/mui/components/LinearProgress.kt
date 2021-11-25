package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.LinearProgress
import mui.material.LinearProgressProps
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class UmLinearProgressColor {
    primary, secondary
}

@Suppress("EnumEntryName")
enum class UmLinearProgressVariant {
    determinate, indeterminate, buffer, query
}


fun RBuilder.umLinearProgress(
    value: Double? = null,
    valueBuffer: Double? = null,
    variant: UmLinearProgressVariant = UmLinearProgressVariant.indeterminate,
    color: UmLinearProgressColor = UmLinearProgressColor.primary,
    className: String? = null,
    handler: StyledHandler<LinearProgressProps>?
) = createStyledComponent(LinearProgress, className, handler){
    attrs.color = color.toString()
    attrs.variant = variant.toString()
    attrs.value = value
    attrs.valueBuffer = valueBuffer
}