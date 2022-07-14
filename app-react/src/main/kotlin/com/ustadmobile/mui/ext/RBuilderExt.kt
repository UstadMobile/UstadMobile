package com.ustadmobile.mui.ext

import csstype.ClassName
import react.*
import styled.StyledElementBuilder
import styled.StyledHandler

fun <P : PropsWithClassName> RBuilder.createStyledComponent(
    componentType: ComponentType<P>,
    className: String? = null,
    handler: StyledHandler<P>? = null,
    propsHandler: StyledHandler<P>? = null
): ReactElement<P> {
    val element = with(StyledElementBuilder(componentType)) {
        if(className != null)
            attrs.className = ClassName(className)

        if (propsHandler != null) propsHandler(this)
        if (handler != null) handler(this)
        create()
    }
    child(element)

    return element.unsafeCast<ReactElement<P>>()
}