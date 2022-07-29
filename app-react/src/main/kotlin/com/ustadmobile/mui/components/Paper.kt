package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.Paper
import mui.material.PaperProps
import mui.material.PaperVariant
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umPaper(
    elevation: Int = 2,
    square: Boolean = false,
    variant: PaperVariant = PaperVariant.elevation,
    className: String? = null,
    handler: StyledHandler<PaperProps>? = null
) = convertFunctionalToClassElement(Paper, className, handler) {
    attrs.elevation = elevation
    attrs.square = square
    attrs.variant = variant
}