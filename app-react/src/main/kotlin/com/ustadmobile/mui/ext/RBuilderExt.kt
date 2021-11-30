package com.ustadmobile.mui.ext

import react.*
import styled.StyledElementBuilder
import styled.StyledHandler

fun <P : PropsWithClassName> RBuilder. createStyledComponent(
    componentType: ComponentType<P>,
    className: String? = null,
    handler: StyledHandler<P>? = null,
    propsHandler: StyledHandler<P>? = null
): ReactElement {
    val element = with(StyledElementBuilder(componentType)) {
        attrs.className = className
        if (propsHandler != null) propsHandler(this)
        if (handler != null) handler(this)
        create()
    }
    child(element)
    return element
}