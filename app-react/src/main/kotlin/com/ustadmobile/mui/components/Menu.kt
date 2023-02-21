package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.Menu
import mui.material.MenuProps
import mui.material.MenuVariant
import org.w3c.dom.Element
import react.RBuilder
import styled.StyledHandler
import kotlin.js.json

fun RBuilder.umMenu(
    open: Boolean,
    anchorElement: Element? = null,
    onClose: (() -> Unit)? = null,
    className: String? = null,
    variant: MenuVariant = MenuVariant.menu,
    handler: StyledHandler<MenuProps>? = null
) = convertFunctionalToClassElement(Menu, className, handler){
    attrs.open = open
    attrs.onClose = {
        onClose?.invoke()
    }
    attrs.MenuListProps = json("aria-labelledby" to "basic-button").asDynamic()
    attrs.variant = variant
    anchorElement?.let{
        attrs.asDynamic().anchorEl = it
    }
}