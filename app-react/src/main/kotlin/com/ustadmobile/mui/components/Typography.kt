package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Typography
import mui.material.TypographyProps
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class TypographyAlign {
    inherit, left, center, right, justify
}

@Suppress("EnumEntryName")
enum class TypographyColor {
    initial, inherit, primary, secondary, textPrimary, textSecondary, error
}

@Suppress("EnumEntryName")
enum class TypographyVariant {
    h1, h2, h3, h4, h5, h6, body1, body2, subtitle1, subtitle2, caption, button, overline, srOnly, inherit
}


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
) = createStyledComponent(Typography, className, handler) {
    attrs.asDynamic().align = align.toString()
    attrs.gutterBottom = gutterBottom
    attrs.noWrap = noWrap
    component?.let {
        attrs.asDynamic().component = it
    }
    attrs.paragraph = paragraph
    attrs.asDynamic().variant = variant.toString()
    text?.let {
        childList.add(ReactNode(it))
    }
}