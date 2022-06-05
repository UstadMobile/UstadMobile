package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.ListProps
import react.RBuilder
import react.ReactElement
import styled.StyledHandler

fun RBuilder.umList(
    dense: Boolean = false,
    disablePadding: Boolean = false,
    subheader: ReactElement? = null,
    component: String = "ul",
    className: String? = null,
    handler: StyledHandler<ListProps>? = null
) = createStyledComponent(mui.material.List, className, handler) {
    attrs.asDynamic().component = component
    attrs.dense = dense
    attrs.disablePadding = disablePadding
    subheader?.let { attrs.subheader = subheader }
}