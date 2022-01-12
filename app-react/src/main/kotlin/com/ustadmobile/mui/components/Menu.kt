package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Menu
import mui.material.MenuProps
import org.w3c.dom.Element
import react.RBuilder
import styled.StyledHandler
import kotlin.js.json

enum class MenuVariant{
    menu, selectedMenu
}

fun RBuilder.umMenu(
    open: Boolean,
    anchorElement: Element? = null,
    onClose: (() -> Unit)? = null,
    className: String? = null,
    variant: MenuVariant = MenuVariant.menu,
    handler: StyledHandler<MenuProps>? = null
) = createStyledComponent(Menu, className, handler){
    attrs.open = open
    attrs.onClose = {
        onClose?.invoke()
    }
    attrs.asDynamic().MenuListProps = json("aria-labelledby" to "basic-button")
    attrs.variant = variant.toString()
    anchorElement?.let{
        attrs.asDynamic().anchorEl = it
    }
}