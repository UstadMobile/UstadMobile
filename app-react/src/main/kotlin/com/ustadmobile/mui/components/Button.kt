package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import mui.material.Button
import mui.material.ButtonProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler

@Suppress("EnumEntryName")
enum class ButtonSize {
    small, medium, large
}

@Suppress("EnumEntryName")
enum class ButtonVariant {
    text, outlined, contained
}

fun RBuilder.umButton(
    caption: String,
    color: UMColor = UMColor.default,
    variant: ButtonVariant? = null,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: ButtonSize = ButtonSize.medium,
    className: String? = null,
    handler: StyledHandler<ButtonProps>? = null
) = createStyledComponent(Button, className, handler) {
    attrs.color = color.toString()
    attrs.disabled = disabled
    attrs.onClick = {
        onClick?.invoke(it.nativeEvent)
    }
    attrs.size = size.toString()
    attrs.variant = variant.toString()
    childList.add(ReactNode(caption))
}