package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import react.ElementType
import react.Props
import react.RBuilder
import react.ReactNode
import react.dom.html.HTMLAttributes
import styled.StyledHandler
import styled.StyledProps

external interface UMTabsProps: TabsProps, StyledProps {}

fun RBuilder.umTabs(
    value: Any = false, // false means none selected
    centered: Boolean = false,
    variant: TabsVariant = TabsVariant.standard,
    orientation: Orientation = Orientation.horizontal,
    indicatorColor: TabsIndicatorColor = TabsIndicatorColor.secondary,
    textColor: TabsTextColor = TabsTextColor.inherit,
    tabIndicatorProps: HTMLAttributes<HTMLDivElement>? = null,
    scrollButtons: TabsScrollButtons = TabsScrollButtons.auto,
    scrollButtonComponent: ElementType<Props>? = null,
    onChange: ((event: Event, indexValue: Any) -> Unit)? = null,
    action: ((actions: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<UMTabsProps>? = null
) = convertFunctionalToClassElement(Tabs, className, handler) {
    action?.let { attrs.asDynamic().action = it }
    attrs.centered = centered
    attrs.indicatorColor = indicatorColor
    onChange?.let { attrs.asDynamic().onChange = it }
    attrs.orientation = orientation
    scrollButtonComponent?.let { attrs.ScrollButtonComponent = it }
    attrs.scrollButtons = scrollButtons
    tabIndicatorProps?.let { attrs.TabIndicatorProps = it }
    attrs.textColor = textColor
    attrs.value = value
    attrs.variant = variant
}

fun RBuilder.umTab(
    label: String,
    value: Any = label,
    icon: String? = null,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<TabProps>? = null
) = convertFunctionalToClassElement(Tab, className, handler) {
    attrs.disabled = disabled
    icon?.let { attrs.icon = ReactNode(it) }
    attrs.label = ReactNode(label)
    attrs.value = value
}