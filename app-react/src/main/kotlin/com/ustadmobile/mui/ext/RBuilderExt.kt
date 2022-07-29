package com.ustadmobile.mui.ext

import csstype.ClassName
import react.ComponentType
import react.PropsWithClassName
import react.RBuilder
import react.ReactElement
import styled.StyledElementBuilder
import styled.StyledHandler

/**
 * convertFunctionalToClassElement, convert functional and un-styled react elements
 * to styled and class elements which makes those elements re-usable as it provides
 * more flexibility when dealing with class components.
 *
 * Our current apps architecture requires class components over functional components
 * that's why we do convert functional to class react elements.
 *
 * Read more: https://reactjs.org/docs/components-and-props.html
 * More to read: https://www.twilio.com/blog/react-choose-functional-components
 *
 */
fun <P : PropsWithClassName> RBuilder. convertFunctionalToClassElement(
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