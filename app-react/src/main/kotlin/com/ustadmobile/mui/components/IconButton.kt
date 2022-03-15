package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.Util.stopEventPropagation
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

fun RBuilder.umIconButton(
    iconName: String? = null,
    color: UMColor = UMColor.default,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: IconButtonSize = IconButtonSize.medium,
    iconColor: IconColor? = null,
    id: String? = null,
    edge: IconEdge? = null,
    className: String? = null,
    handler: StyledHandler<IconButtonProps>? = null
) = createStyledComponent(IconButton, className, handler) {
    attrs.color = color.toString()
    attrs.disabled = disabled
    attrs.disableFocusRipple = disabled
    attrs.size = size.toString()
    edge?.let { attrs.edge = it.toString() }
    attrs.onClick = {
        stopEventPropagation(it)
        onClick?.invoke(it.nativeEvent)
    }

    id?.let { attrs.id = it }

    var colorToApply = iconColor

    if (colorToApply == null) {
        colorToApply = when (color) {
            UMColor.inherit -> IconColor.inherit
            UMColor.default -> IconColor.action
            UMColor.secondary -> IconColor.secondary
            UMColor.primary -> IconColor.primary
            else -> IconColor.inherit
        }
    }

    if (iconName != null) {
        val fontSize = when (size) {
            IconButtonSize.small -> IconFontSize.small
            IconButtonSize.medium -> IconFontSize.default
        }

        umIcon(iconName, color = colorToApply, fontSize = fontSize)
    }
}