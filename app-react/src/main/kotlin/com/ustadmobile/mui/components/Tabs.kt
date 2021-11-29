package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Tab
import mui.material.TabProps
import mui.material.Tabs
import mui.material.TabsProps
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import react.ElementType
import react.Props
import react.RBuilder
import react.ReactElement
import react.dom.html.HTMLAttributes
import styled.StyledHandler
import styled.StyledProps

@Suppress("EnumEntryName")
enum class TabTextColor {
    secondary, primary, inherit
}

@Suppress("EnumEntryName")
enum class TabScrollButtons {
    auto, desktop, on, off
}

@Suppress("EnumEntryName")
enum class TabIndicatorColor {
    secondary, primary
}

@Suppress("EnumEntryName")
enum class TabVariant {
    standard, scrollable, fullWidth
}

@Suppress("EnumEntryName")
enum class TabOrientation {
    horizontal, vertical
}

external interface UMTabsProps: TabsProps, StyledProps

fun RBuilder.umTabs(
    value: Any = false, // false means none selected
    centered: Boolean = false,
    variant: TabVariant = TabVariant.standard,
    orientation: TabOrientation = TabOrientation.horizontal,
    indicatorColor: TabIndicatorColor = TabIndicatorColor.secondary,
    textColor: TabTextColor = TabTextColor.inherit,
    tabIndicatorProps: HTMLAttributes<HTMLDivElement>? = null,
    scrollButtons: TabScrollButtons = TabScrollButtons.auto,
    scrollButtonComponent: ElementType<Props>? = null,
    onChange: ((event: Event, indexValue: Any) -> Unit)? = null,
    action: ((actions: Any) -> Unit)? = null,
    className: String? = null,
    handler: StyledHandler<UMTabsProps>? = null
) = createStyledComponent(Tabs, className, handler) {
    action?.let { attrs.asDynamic().action = it }
    attrs.centered = centered
    attrs.asDynamic().indicatorColor = indicatorColor
    onChange?.let { attrs.asDynamic().onChange = it }
    attrs.asDynamic().orientation = orientation
    scrollButtonComponent?.let { attrs.ScrollButtonComponent = it }
    attrs.asDynamic().scrollButtons = scrollButtons
    tabIndicatorProps?.let { attrs.TabIndicatorProps = it }
    attrs.asDynamic().textColor = textColor
    attrs.asDynamic().value = value
    attrs.asDynamic().variant = variant
}

fun RBuilder.umTab(
    label: ReactElement? = null,
    value: Any? = null,
    icon: ReactElement? = null,
    disabled: Boolean = false,
    disableRipple: Boolean? = null,
    disableFocusRipple: Boolean? = null,
    wrapped: Boolean = false,

    className: String? = null,
    handler: StyledHandler<TabProps>? = null
) = createStyledComponent(Tab, className, handler) {
    attrs.disabled = disabled
    disableFocusRipple?.let { attrs.disableFocusRipple = it }
    disableRipple?.let { attrs.asDynamic().disableRipple = it }
    icon?.let { attrs.icon = icon }
    label?.let {attrs.label = label }
    value?.let { attrs.value = value }
    attrs.wrapped = wrapped
}

fun RBuilder.umTab(
    label: String,
    value: Any = label,
    icon: ReactElement? = null,
    disabled: Boolean = false,
    className: String? = null,
    handler: StyledHandler<TabProps>? = null
) = createStyledComponent(Tab, className, handler) {
    attrs.disabled = disabled
    icon?.let { attrs.icon = icon }
    attrs.label = label.asDynamic()
    attrs.value = value
}