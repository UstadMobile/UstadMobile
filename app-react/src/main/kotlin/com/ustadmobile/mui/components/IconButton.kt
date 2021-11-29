package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.IconButton
import mui.material.IconButtonProps
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class IconButtonSize {
    small, medium
}

enum class IconEdge {
    start, end
}

fun RBuilder.mIconButton(
    iconName: String? = null,
    color: UMColor = UMColor.default,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: IconButtonSize = IconButtonSize.medium,
    iconColor: IconColor? = null,
    edge: IconEdge? = null,
    className: String? = null,
    handler: StyledHandler<IconButtonProps>? = null
) = createStyledComponent(IconButton, className, handler) {
    attrs.asDynamic().color = color
    attrs.disabled = disabled
    attrs.disableFocusRipple = disabled
    edge?.let { attrs.asDynamic().edge = it }
    onClick?.let { attrs.asDynamic().onClick = onClick }

    var colorToApply = iconColor
    // If the iconColor is null, we shall map to the button color if we can
    if (colorToApply == null) {
        colorToApply = when (color) {
            UMColor.inherit -> IconColor.inherit
            UMColor.default -> IconColor.action
            UMColor.secondary -> IconColor.secondary
            UMColor.primary -> IconColor.primary
        }
    }
    attrs.asDynamic().size = size.toString()
    if (iconName != null) {
        val fontSize = when (size) {
            IconButtonSize.small -> IconFontSize.small
            IconButtonSize.medium -> IconFontSize.default
        }

        umIcon(iconName, color = colorToApply, fontSize = fontSize)
    }
}