package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.Util
import mui.material.ContainerProps
import mui.material.ListItem
import mui.material.ListItemAlignItems
import mui.material.ListItemProps
import org.w3c.dom.events.Event
import react.RBuilder
import styled.StyledHandler


fun RBuilder.umListItem(
    selected: Boolean = false,
    key: String? = null,
    alignItems: ListItemAlignItems = ListItemAlignItems.center,
    divider: Boolean = true,
    onClick: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<ListItemProps>? = null
) = umListItem(
    button = true,
    selected = selected,
    key = key,
    alignItems = alignItems,
    divider = divider,
    onClick = onClick,
    className = className) {
    if (handler != null) handler()
}


fun RBuilder.umListItemWithIcon(
    iconName: String,
    primaryText: String,
    secondaryText: String? = null,
    selected: Boolean = false,
    key: String? = null,
    id: String? = null,
    alignItems: ListItemAlignItems = ListItemAlignItems.center,
    divider: Boolean = true,
    useAvatar: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<ListItemProps>? = null
) = umListItem(
    button = true,
    selected = selected,
    id = id,
    key = key,
    alignItems = alignItems,
    divider = divider,
    onClick = onClick,
    className = className) {

    if (useAvatar) {
        umListItemAvatar { umAvatar { umIcon(iconName) } }
    } else {
        umListItemIcon(iconName)
    }
    umListItemText(primaryText, secondaryText)

    // We don't call setStyledPropsAndRunHandler as this is called in the original mListItem above (but the handler below is not)
    if (handler != null) handler()
}

fun RBuilder.umListItem(
    button: Boolean = false,
    component: String? = null,
    selected: Boolean = false,
    key: String? = null,
    id: String? = null,
    alignItems: ListItemAlignItems = ListItemAlignItems.center,
    containerProps: ContainerProps? = null,
    dense: Boolean = false,
    disableGutters: Boolean = false,
    divider: Boolean = false,
    autoFocus: Boolean = false,
    onClick: ((Event) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<ListItemProps>? = null
) = convertFunctionalToClassElement(ListItem, className, handler) {
    attrs.alignItems = alignItems
    attrs.autoFocus = autoFocus
    attrs.asDynamic().button = button
    component?.let { attrs.asDynamic().component = component }
    containerProps?.let { attrs.ContainerProps = containerProps }
    attrs.dense = dense
    attrs.disableGutters = disableGutters
    attrs.divider = divider
    attrs.onClick = {
        Util.stopEventPropagation(it)
        onClick?.invoke(it.nativeEvent)
    }
    id?.let { attrs.asDynamic().id = it }
    attrs.selected = selected
    key?.let { attrs.asDynamic().key = key }
    attrs.selected = selected
}