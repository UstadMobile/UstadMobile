package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import mui.material.ListProps
import react.RBuilder
import styled.StyledHandler

fun RBuilder.umList(
    dense: Boolean = false,
    disablePadding: Boolean = false,
    component: String = "ul",
    className: String? = null,
    handler: StyledHandler<ListProps>? = null
) = convertFunctionalToClassElement(mui.material.List, className, handler) {
    attrs.asDynamic().component = component
    attrs.dense = dense
    attrs.disablePadding = disablePadding
}