package com.ustadmobile.mui.components

import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.util.ext.assignPropsTo
import js.objects.jso
import mui.material.TextField
import mui.material.TextFieldProps
import react.FC
import react.dom.onChange
import react.useEffect
import react.useState
import web.html.HTMLInputElement
import web.html.InputMode

/**
 * Properties will be copied over to the TextField itself
 */
external interface UstadNullableNumberTextFieldProps : TextFieldProps {

    var numValue: Float?

    var onChange: (Float?) -> Unit

}

val UstadNullableNumberTextField = FC<UstadNullableNumberTextFieldProps> { props ->
    var rawValue: String by useState {
        props.numValue?.toDisplayString() ?: ""
    }

    useEffect(props.numValue) {
        if(rawValue.toFloatOrNull() != props.numValue) {
            console.log("nullablenum: rawValue=$rawValue numValue=${props.numValue} updating raw value")
            rawValue = props.numValue?.toDisplayString() ?: ""
        }
    }

    TextField {
        props.assignPropsTo(this) {
            it != "value" && it != "onChange" && it != "numValue" && it != "numValueIfBlank"
        }

        if(props.inputProps == null) {
            inputProps = jso {
                inputMode = InputMode.numeric
            }
        }

        value = rawValue

        onChange = { evt ->
            val text = evt.target.unsafeCast<HTMLInputElement>().value


            val filteredText = text.filter { it.isDigit() || it == '.' }
            rawValue = filteredText

            console.log("nullablenum: onChange: text='$text' filtered text='$filteredText'")

            val floatVal = filteredText.toFloatOrNull()
            props.onChange(floatVal)
        }
    }

}