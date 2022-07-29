package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.*
import react.RBuilder
import styled.StyledHandler


fun RBuilder.umLinearProgress(
    value: Double? = null,
    valueBuffer: Double? = null,
    variant: LinearProgressVariant = LinearProgressVariant.indeterminate,
    color: LinearProgressColor = LinearProgressColor.primary,
    className: String? = null,
    handler: StyledHandler<LinearProgressProps>?
) = convertFunctionalToClassElement(LinearProgress, className, handler){
    attrs.color = color
    attrs.variant = variant
    attrs.value = value
    attrs.valueBuffer = valueBuffer
}

fun RBuilder.umCircularProgress(
    value: Double? = null,
    variant: CircularProgressVariant = CircularProgressVariant.indeterminate,
    color: CircularProgressColor = CircularProgressColor.primary,
    size: Int = 40,
    thickness: Double = 3.6,
    className: String? = null,
    handler: StyledHandler<CircularProgressProps>?
) = convertFunctionalToClassElement(CircularProgress, className, handler){
    attrs.color = color
    attrs.variant = variant
    attrs.value = value
    attrs.size = size
    attrs.thickness = thickness
}