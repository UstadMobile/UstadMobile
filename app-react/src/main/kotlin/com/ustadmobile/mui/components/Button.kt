package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import mui.material.Button
import mui.material.ButtonProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler
import styled.css

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
    color: UMColor = UMColor.secondary,
    variant: ButtonVariant? = null,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: ButtonSize = ButtonSize.medium,
    startIcon: String? = null,
    endIcon: String? = null,
    className: String? = null,
    handler: StyledHandler<ButtonProps>? = null
) = createStyledComponent(Button, className, handler) {
    attrs.color = color.toString()
    attrs.disabled = disabled
    attrs.onClick = {
        onClick?.invoke(it.nativeEvent)
    }
    attrs.size = size.toString()
    startIcon?.let {
        umIcon(it){
            css(StyleManager.startIcon)
        }
    }

    attrs.variant = variant.toString()
    childList.add(ReactNode(caption))
    endIcon?.let {
        umIcon(it){
            css(StyleManager.endIcon)
        }
    }
}