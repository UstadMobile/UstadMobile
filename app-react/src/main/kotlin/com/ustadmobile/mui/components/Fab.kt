package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.Fab
import mui.material.FabProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class FabVariant {
    round, extended
}

fun RBuilder.umFab(
    iconName: String,
    caption: String,
    color: UMColor = UMColor.default,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: ButtonSize = ButtonSize.medium,
    className: String? = null,
    id: String? = null,
    variant: FabVariant = FabVariant.extended,
    handler: StyledHandler<FabProps>? = null
) = createStyledComponent(Fab, className, handler) {
    attrs.color = color.toString()
    attrs.disabled = disabled
    attrs.onClick = {
        onClick?.invoke(it.nativeEvent)
    }
    id?.let{ attrs.id = id }
    attrs.size = size.toString()
    attrs.variant = variant.toString()

    umIcon(iconName)
    childList.add(ReactNode(caption))
}