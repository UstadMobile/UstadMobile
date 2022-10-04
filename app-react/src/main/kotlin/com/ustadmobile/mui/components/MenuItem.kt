package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.MenuItem
import mui.material.MenuItemProps
import org.w3c.dom.events.Event
import react.Props
import react.RBuilder
import styled.StyledHandler


fun RBuilder.umMenuItem(
    primaryText: String,
    secondaryText: String? = null,
    selected: Boolean = false,
    key: String? = null,
    value: String? = null,
    divider: Boolean = false,
    disabled: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<MenuItemProps>? = null
) {
    umMenuItem(selected,
        button = true,
        key = key,
        value = value,
        divider = divider,
        disabled = disabled,
        onClick = onClick,
        className = className) {

        if (secondaryText == null) {
            // Just a simple text child element is all that is required...
            +primaryText
        } else {
            umListItemText(primaryText, secondaryText)
        }
        if (handler != null) handler()
    }
}

fun RBuilder.umMenuItem(
    selected: Boolean = false,
    button: Boolean = false,
    component: String? = null,
    containerComponent: String = "li",
    key: String? = null,
    value: String? = null,
    divider: Boolean = false,
    disabled: Boolean = false,
    containerProps: Props? = null,
    dense: Boolean = false,
    disableGutters: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<MenuItemProps>? = null
) {
    convertFunctionalToClassElement(MenuItem, className, handler) {
        attrs.asDynamic().button = button
        component?.let { attrs.asDynamic().component = it }
        attrs.asDynamic().containerComponent = containerComponent
        containerProps?.let { attrs.asDynamic().containerProps = it }
        attrs.dense = dense
        attrs.disabled = disabled
        attrs.disableGutters = disableGutters
        attrs.divider = divider
        attrs.onClick = {
            onClick?.invoke(it.nativeEvent)
        }
        key?.let { attrs.asDynamic().key = it }
        attrs.selected = selected
        value?.let { attrs.value = it }
    }
}
