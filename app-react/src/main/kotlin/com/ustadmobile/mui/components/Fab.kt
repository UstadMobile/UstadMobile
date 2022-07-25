package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createReUsableComponent
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
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
) = createReUsableComponent(Fab, className, handler) {
    attrs.color = color
    attrs.disabled = disabled
    attrs.onClick = {
        onClick?.invoke(it.nativeEvent)
    }
    id?.let{ attrs.id = id }
    attrs.size = size
    attrs.variant = variant

    umIcon(iconName)
    childList.add(ReactNode(caption))
}