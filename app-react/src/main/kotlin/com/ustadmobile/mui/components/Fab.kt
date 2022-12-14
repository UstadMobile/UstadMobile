package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.*
import mui.material.styles.TypographyVariant
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler


fun RBuilder.umFab(
    iconName: String,
    caption: String,
    color: FabColor = FabColor.default,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: Size = Size.medium,
    className: String? = null,
    id: String? = null,
    variant: FabVariant = FabVariant.extended,
    handler: StyledHandler<FabProps>? = null
) = convertFunctionalToClassElement(Fab, className, handler) {
    attrs.color = color
    attrs.disabled = disabled
    attrs.onClick = {
        onClick?.invoke(it.nativeEvent)
    }
    id?.let{ attrs.id = id }
    attrs.size = size
    attrs.variant = variant
    umIcon(iconName)
    umTypography(caption, variant = TypographyVariant.button)
}