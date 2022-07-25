package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import com.ustadmobile.util.Util.stopEventPropagation
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umIconButton(
    iconName: String? = null,
    color: IconButtonColor = IconButtonColor.default,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: Size = Size.medium,
    iconColor: IconColor = IconColor.inherit,
    id: String? = null,
    edge: IconButtonEdge? = null,
    className: String? = null,
    handler: StyledHandler<IconButtonProps>? = null
) = createReUsableComponent(IconButton, className, handler) {
    attrs.color = color
    attrs.disabled = disabled
    attrs.disableFocusRipple = disabled
    attrs.size = size
    edge?.let { attrs.edge = it}
    attrs.onClick = {
        stopEventPropagation(it)
        onClick?.invoke(it.nativeEvent)
    }

    id?.let { attrs.id = it }

    if (iconName != null) {

        umIcon(iconName, color = iconColor, size = size as IconSize)
    }
}