package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.BottomNavigation
import mui.material.BottomNavigationAction
import mui.material.BottomNavigationActionProps
import mui.material.BottomNavigationProps
import org.w3c.dom.events.Event
import react.RBuilder
import react.ReactNode
import styled.StyledHandler
import styled.StyledProps

external interface UMBottomNavigationActionProps: BottomNavigationActionProps, StyledProps

fun RBuilder.umBottomNavigation(
    value: Any = false,
    showLabels: Boolean = false,
    onChange: ((event: Event, indexValue: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<BottomNavigationProps>? = null
) = convertFunctionalToClassElement(BottomNavigation, className, handler) {
    attrs.onChange = { event, value ->
        onChange?.invoke(event.nativeEvent, value)
    }
    attrs.showLabels = showLabels
    attrs.value = value
}


fun RBuilder.umBottomNavigationAction(
    label: String? = null,
    icon: String? = null,
    showLabel: Boolean? = null,
    value: Any? = null,
    className: String? = null,
    handler: StyledHandler<UMBottomNavigationActionProps>? = null
) = convertFunctionalToClassElement(BottomNavigationAction, className, handler) {
    icon?.let { attrs.icon = umIcon(it)}
    label?.let {it -> attrs.label = ReactNode(it) }
    showLabel?.let { attrs.showLabel = showLabel }
    value?.let { attrs.value = it }
}