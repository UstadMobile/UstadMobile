package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.Util
import mui.material.*
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler
import styled.css


fun RBuilder.umButton(
    label: String,
    color: ButtonColor = ButtonColor.secondary,
    variant: ButtonVariant? = null,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    size: Size = Size.medium,
    startIcon: String? = null,
    endIcon: String? = null,
    id: String = this::class.js.name,
    className: String? = null,
    handler: StyledHandler<ButtonProps>? = null
) = convertFunctionalToClassElement(Button, className, handler) {
    childList.add(ReactNode(label))
    attrs.color = color
    attrs.disabled = disabled
    attrs.size = size
    attrs.variant = variant
    attrs.id = id
    attrs.startIcon = startIcon?.let {
        umIcon(it){
            css(StyleManager.startIcon)
        }
    }
    endIcon?.let {
        umIcon(it){
            css(StyleManager.endIcon)
        }
    }
    attrs.onClick = {
        Util.stopEventPropagation(it)
        onClick?.invoke(it.nativeEvent)
    }
}