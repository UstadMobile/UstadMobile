package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.ListItemText
import mui.material.ListItemTextProps
import react.RBuilder
import react.ReactElement
import styled.StyledHandler

fun RBuilder.umListItemText(
    primary: String,
    secondary: String? = null,
    inset: Boolean = false,
    disableTypography: Boolean = false,
    className: String? = null,
    handler: StyledHandler<ListItemTextProps>? = null
) = listIstItemText(
    primary.asDynamic(),
    secondary?.asDynamic(),
    inset,
    disableTypography,
    className,
    handler)

fun RBuilder.listIstItemText(
    primary: ReactElement? = null,
    secondary: ReactElement? = null,
    inset: Boolean = false,
    disableTypography: Boolean = false,
    className: String? = null,
    handler: StyledHandler<ListItemTextProps>? = null
) = createStyledComponent(ListItemText, className, handler) {
    attrs.disableTypography = disableTypography
    attrs.inset = inset
    primary?.let { attrs.primary = primary }
    secondary?.let { attrs.secondary = secondary }
}
