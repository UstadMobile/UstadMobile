package com.ustadmobile.mui.ext

import csstype.ClassName
import react.ComponentType
import react.PropsWithClassName
import react.RBuilder
import react.ReactElement
import styled.StyledElementBuilder
import styled.StyledHandler

/**
 * createReUsableComponent, convert functional and un-styled components
 * to styled ReactElements which makes components re-usable and it provide
 * more flexibility and it's a robust way of dealing with React components.
 *
 * There are react components (i.e TextFiled, Button, Chips,Avatar e.tc ) which requires complex
 * ReactNode as children, with functional components that can't be achieved since components are
 * being treated as functions instead of ReactNodes.
 *
 * In React JS, this is handled easily, since props are functional by default
 * while in React Kotlin props are variables.
 * i.e in JS <Component prop = { ....put react-node or functional component }/>
 *
 * Read more https://mui.com/system/styled/
 */
fun <P : PropsWithClassName> RBuilder. createReUsableComponent(
    componentType: ComponentType<P>,
    className: String? = null,
    handler: StyledHandler<P>? = null,
    propsHandler: StyledHandler<P>? = null
): ReactElement<*> {
    val element = with(StyledElementBuilder(componentType)) {
        attrs.className = className?.let { _class -> ClassName(_class) }
        if (propsHandler != null) propsHandler(this)
        if (handler != null) handler(this)
        create()
    }
    child(element)
    return element
}