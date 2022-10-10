package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import kotlinx.css.WhiteSpace
import kotlinx.css.whiteSpace
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.TypographyProps
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.ReactNode
import styled.StyledHandler
import styled.css


fun RBuilder.umTypography(
    text: String? = null,
    variant: TypographyVariant = TypographyVariant.body1,
    align: TypographyAlign = TypographyAlign.left,
    gutterBottom: Boolean = false,
    noWrap: Boolean = false,
    component: String? = null,
    paragraph: Boolean = false,
    className: String? = null,
    handler: StyledHandler<TypographyProps>? = null
) = convertFunctionalToClassElement(Typography, className, handler) {
    attrs.asDynamic().align = align.toString()
    attrs.gutterBottom = gutterBottom
    attrs.noWrap = noWrap
    component?.let {
        attrs.asDynamic().component = it
    }
    attrs.paragraph = paragraph
    attrs.variant = variant
    text?.let {
        childList.add(ReactNode(it))
    }
    css{
        whiteSpace = WhiteSpace.preLine
    }
}