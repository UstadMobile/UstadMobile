package com.ustadmobile.mui.ext

import react.ComponentType
import react.PropsWithClassName
import react.RBuilder
import styled.StyledElementBuilder
import styled.StyledHandler

fun <P : PropsWithClassName> RBuilder.createStyledComponent(
    componentType: ComponentType<P>,
    className: String? = null,
    handler: StyledHandler<P>? = null,
    propsHandler: StyledHandler<P>? = null
) {
    child(with(StyledElementBuilder(componentType)) {
        attrs.className = className
        if (propsHandler != null) propsHandler(this)
        if (handler != null) handler(this)
        create()
    })
}