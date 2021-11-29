package com.ustadmobile.mui.components

import Breakpoint
import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Hidden
import mui.material.HiddenProps
import react.RBuilder
import styled.StyledHandler
import styled.StyledProps

@Suppress("EnumEntryName")
enum class HiddenImplementation {
    js, css
}

external interface UMHiddenProps : HiddenProps, StyledProps

fun RBuilder.umHidden(
    only: Array<Breakpoint> = emptyArray(),
    xsUp: Boolean = false,
    smUp: Boolean = false,
    mdUp: Boolean = false,
    lgUp: Boolean = false,
    xlUp: Boolean = false,
    xsDown: Boolean = false,
    smDown: Boolean = false,
    mdDown: Boolean = false,
    lgDown: Boolean = false,
    xlDown: Boolean = false,
    className: String? = null,
    implementation: HiddenImplementation = HiddenImplementation.js,
    initialWidth: Breakpoint? = null,
    handler: StyledHandler<UMHiddenProps>
) = createStyledComponent(Hidden, className, handler) {
    attrs.implementation = implementation.toString()
    initialWidth?.let { attrs.initialWidth = it }
    attrs.lgDown = lgDown
    attrs.lgUp = lgUp
    attrs.mdDown = mdDown
    attrs.mdUp = mdUp
    attrs.only = only
    attrs.smDown = smDown
    attrs.smUp = smUp
    attrs.xlDown = xlDown
    attrs.xlUp = xlUp
    attrs.xsDown = xsDown
    attrs.xsUp = xsUp
}