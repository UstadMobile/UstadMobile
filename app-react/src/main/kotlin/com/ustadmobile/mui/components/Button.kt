package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.Util
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler
import styled.css


fun RBuilder.umButton(
    caption: String,
    color: ButtonColor = ButtonColor.secondary,
    variant: ButtonVariant? = null,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: Size = Size.medium,
    startIcon: String? = null,
    endIcon: String? = null,
    id: String? = this::class.js.name,
    className: String? = null,
    handler: StyledHandler<ButtonProps>? = null
) = createStyledComponent(Button, className, handler) {
    attrs.color = color
    attrs.disabled = disabled
    attrs.size = size
    startIcon?.let {
        umIcon(it){
            css(StyleManager.startIcon)
        }
    }

    attrs.variant = variant
    childList.add(ReactNode(caption))
    endIcon?.let {
        umIcon(it){
            css(StyleManager.endIcon)
        }
    }
    attrs.onClick = {
        Util.stopEventPropagation(it)
        onClick?.invoke(it.nativeEvent)
    }
    id?.let{ attrs.id = it }
}