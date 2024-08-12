package com.ustadmobile.mui.components

import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.util.ext.assignPropsTo
import js.objects.jso
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

    /**
     * The numerical value that will be used if the textfield is blank. If not set, defaults to 0
     */
    var numValueIfBlank: Float?

    var onChange: (Float) -> Unit

}

/**
 * The effective unset value (e.g. numerical value when the text string is blank)
 */
private val UstadNumberTextFieldProps.effectiveValueIfBlank: Float
    get() = numValueIfBlank ?: 0.toFloat()

val UstadNumberTextField = FC<UstadNumberTextFieldProps> { props ->

    var rawValue by useState {
        if(props.numValue != props.effectiveValueIfBlank) props.numValue.toDisplayString() else ""
    }

    //If props change to something other than what we have set, change rawValue
    useEffect(props.numValue) {
        if(props.numValue != (rawValue.toFloatOrNull() ?: props.effectiveValueIfBlank)) {
            //When the value has been reset to the numValueIfBlank, then make the text blank
            rawValue = if(props.numValue == props.numValueIfBlank) {
                ""
            }else {
                props.numValue.toString()
            }
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

            val filteredText = text.filter { it.isDigit() || it == '-' || it == '.' }
            rawValue = filteredText
            val floatVal = filteredText.toFloatOrNull() ?: props.effectiveValueIfBlank
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