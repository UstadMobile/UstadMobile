package com.ustadmobile.mui.components

import com.ustadmobile.util.ext.assignPropsTo
import mui.material.StandardTextFieldProps
import mui.material.TextField
import mui.material.TextFieldProps
import react.FC
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.useEffect
import react.useState
import web.html.HTMLElement
import web.html.HTMLInputElement

/**
 * React requires the update to a textfield to be done synchronously. Changes made by the ViewModel
 * are delivered via an async flow.
 *
 * Normally this should be done by using Dispatchers.Main.immediate to deliver events. However, when
 * a textfield is inside a VirtualList, that can't be done because of the use of state and effect
 * by the virtuallist.
 */
val UstadTextField = FC<TextFieldProps> { props ->
    val propsTextVal = props.value?.toString() ?: ""
    var textFieldValue by useState(propsTextVal)

    /*
     * If the ViewModel changes the value of this field (e.g. change not caused by an input event)
     * then update the state variable to force this to take effect
     */
    useEffect(propsTextVal) {
        if(textFieldValue != propsTextVal)
            textFieldValue = propsTextVal
    }

    TextField {
        props.assignPropsTo(
            receiver = this,
            filter = { it != "value" && it != "onChange" }
        )

        value = textFieldValue

        onChange = { evt ->
            val text = evt.target.unsafeCast<HTMLInputElement>().value
            textFieldValue = text

            props.unsafeCast<StandardTextFieldProps>().onChange?.invoke(
                evt.unsafeCast<ChangeEvent<HTMLElement>>()
            )
        }
    }
}