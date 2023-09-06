package com.ustadmobile.mui.components

import js.core.jso
import kotlinext.js.getOwnPropertyNames
import mui.material.*
import mui.system.responsive
import react.*
import react.dom.onChange
import web.html.*
import kotlin.random.Random

/**
 * Properties will be copied over to the TextField itself
 */
external interface UstadNumberTextFieldProps : TextFieldProps {

    var numValue: Float

    var onChange: (Float) -> Unit

}

val UstadNumberTextField = FC<UstadNumberTextFieldProps> { props ->

    var rawValue by useState {
        if(props.numValue != 0.toFloat()) props.numValue.toString() else ""
    }

    //If props change to something other than what we have set, change rawValue
    useEffect(props.numValue) {
        if(props.numValue != (rawValue.toIntOrNull()?.toFloat() ?: 0)) {
            rawValue = props.numValue.toString()
        }
    }

    TextField {
        props.getOwnPropertyNames().filter { it != "value" && it != "onChange" }.forEach { propName ->
            asDynamic()[propName] = props.asDynamic()[propName]
        }

        if(props.inputProps == null) {
            inputProps = jso {
                inputMode = InputMode.numeric
            }
        }

        value = rawValue

        onChange = {
            val text = it.target.unsafeCast<HTMLInputElement>().value

            val filteredText = text.filter { it.isDigit() }
            rawValue = filteredText
            val floatVal = filteredText.toFloatOrNull() ?: 0.toFloat()
            props.onChange(floatVal)
        }

    }

}

val UstadNumberTextFieldPreview = FC<Props> {

    var aNumber by useState { 0.toFloat() }

    Stack {
        spacing = responsive (3)

        UstadNumberTextField {
            numValue = aNumber
            id = "numberfield"
            label = ReactNode("Phone")
            placeholder = "Phone"
            disabled = false
            asDynamic().InputProps = jso<InputBaseProps> {
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    + ("point")
                }
            }

            onChange = {
                aNumber = it
            }
        }

        //Test to ensure that property changes will take effect as expected.
        Button {
            variant = ButtonVariant.outlined
            onClick = {
                aNumber = Random.nextInt(1, 6).toFloat()
            }
            + "Roll Dice"
        }
    }


}