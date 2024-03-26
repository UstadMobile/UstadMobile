package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.util.ext.assignPropsTo
import js.objects.jso
import mui.icons.material.Visibility
import mui.icons.material.VisibilityOff
import mui.material.IconButton
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.InputBaseProps
import mui.material.StandardTextFieldProps
import mui.material.TextField
import mui.material.TextFieldProps
import react.FC
import react.create
import react.dom.aria.ariaLabel
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.useEffect
import react.useState
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.InputType

/**
 * Component for showing a password, which allows the user to toggle visibility on and off
 */
val UstadPasswordTextField = FC<TextFieldProps> { props ->
    val propsTextVal = props.value?.toString() ?: ""
    var textFieldValue by useState(propsTextVal)
    var passwordVisible by useState(false)
    val strings = useStringProvider()

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

        type = if (passwordVisible) {
            InputType.text
        } else {
            InputType.password
        }

        asDynamic().InputProps = jso<InputBaseProps> {
            endAdornment = InputAdornment.create {
                position = InputAdornmentPosition.end
                IconButton {
                    ariaLabel = strings[MR.strings.toggle_visibility]
                    onClick = {
                        passwordVisible = !passwordVisible
                    }

                    if (passwordVisible) {
                        VisibilityOff()
                    } else {
                        Visibility()
                    }
                }
            }

        }
    }
}