package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.BottomNavigation
import mui.material.BottomNavigationAction
import mui.material.BottomNavigationActionProps
import mui.material.BottomNavigationProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactElement
import styled.StyledHandler
import styled.StyledProps


external interface UMBottomNavigationProps: BottomNavigationProps, StyledProps

fun RBuilder.umBottomNavigation(
    value: Any = false,
    showLabels: Boolean = false,
    component: String = "div",
    onChange: ((event: Event, indexValue: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<BottomNavigationProps>? = null
) {
    createStyledComponent(BottomNavigation, className, handler) {
        attrs.asDynamic().component = component
        attrs.onChange = { event, index ->
            onChange?.invoke(event.nativeEvent, index)
        }
        attrs.showLabels = showLabels
        attrs.value = value
    }
}

external interface UMBottomNavigationActionProps: BottomNavigationActionProps, StyledProps


fun RBuilder.umBottomNavigationAction(
    label: String,
    icon: ReactElement? = null,
    showLabel: Boolean? = null,
    value: Any? = null,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<UMBottomNavigationActionProps>? = null
) = umBottomNavigationAction(label.asDynamic() as ReactElement?, icon, showLabel, value, disabled, className, handler)

fun RBuilder.umBottomNavigationAction(
    label: ReactElement? = null,
    icon: ReactElement? = null,
    showLabel: Boolean? = null,
    value: Any? = null,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<UMBottomNavigationActionProps>? = null
) = createStyledComponent(BottomNavigationAction, className, handler) {
    attrs.asDynamic().disabled = disabled
    icon?.let { attrs.icon = icon }
    label?.let {it -> attrs.label = it }
    showLabel?.let { attrs.showLabel = showLabel }
    value?.let { attrs.value = it }
}