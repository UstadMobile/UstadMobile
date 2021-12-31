package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.CircularProgress
import mui.material.CircularProgressProps
import mui.material.LinearProgress
import mui.material.LinearProgressProps
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class ProgressVariant {
    determinate, indeterminate, buffer, query
}


fun RBuilder.umLinearProgress(
    value: Double? = null,
    valueBuffer: Double? = null,
    variant: ProgressVariant = ProgressVariant.indeterminate,
    color: UMColor = UMColor.primary,
    className: String? = null,
    handler: StyledHandler<LinearProgressProps>?
) = createStyledComponent(LinearProgress, className, handler){
    attrs.color = color.toString()
    attrs.variant = variant.toString()
    attrs.value = value
    attrs.valueBuffer = valueBuffer
}

fun RBuilder.umCircularProgress(
    value: Double? = null,
    variant: ProgressVariant = ProgressVariant.indeterminate,
    color: UMColor = UMColor.primary,
    size: Int = 40,
    thickness: Double = 3.6,
    className: String? = null,
    handler: StyledHandler<CircularProgressProps>?
) = createStyledComponent(CircularProgress, className, handler){
    attrs.color = color.toString()
    attrs.variant = variant.toString()
    attrs.value = value
    attrs.size = size
    attrs.thickness = thickness
}