package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.ListItemText
import mui.material.ListItemTextProps
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

fun RBuilder.umListItemText(
    primary: String? = null,
    secondary: String? = null,
    inset: Boolean = false,
    disableTypography: Boolean = false,
    className: String? = null,
    handler: StyledHandler<ListItemTextProps>? = null
) = convertFunctionalToClassElement(ListItemText, className, handler) {
    attrs.disableTypography = disableTypography
    attrs.inset = inset
    primary?.let { _primary -> attrs.primary = ReactNode(_primary) }
    secondary?.let { _secondary -> attrs.secondary = ReactNode(_secondary) }
}
