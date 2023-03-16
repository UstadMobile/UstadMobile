package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.readOnly
import js.core.jso
import mui.material.*
import react.*
import react.dom.onChange
import web.html.InputMode
import web.html.InputType

external interface UstadNumberTextEditFieldProps : Props {

    /**
     * Text value in the field
     */
    var value: Int?

    /**
     * Label to show the user
     */
    var label: ReactNode?


    var placeholder: String?


    var endAdornment: ReactNode?

    /**
     * Error text, if any. Default is null. Null indicates no error. If there is an error, the field
     * will be in error state (e.g. red) and the text will be shown underneath
     */
    var error: String?


    var fullWidth: Boolean

    /**
     * Enabled or disabled
     */
    var disabled: Boolean?

    /**
     * onChange event handler
     */
    var onChange: (Int) -> Unit

    /**
     * InputProps setter functions - can be used to add adornments, set the input type, etc.
     */
    var inputProps: InputBaseComponentProps?


    var type: InputType?

}

val UstadNumberTextEditField = FC<UstadNumberTextEditFieldProps> { props ->

    var rawValue by useState {
        if(props.value != 0) props.value.toString() else ""
    }

    TextField {

        + props

        variant = FormControlVariant.outlined
        value = rawValue

        onChange = {
            val text = (it.target.asDynamic().value)?.toString() ?: ""

            val filteredText = text.filter { it.isDigit() }
            rawValue = filteredText
            val intVal = filteredText.toIntOrNull() ?: 0
            props.onChange(intVal)
        }

    }

}

val UstadNumberTextEditFieldPreview = FC<Props> {

    var aNumber by useState { 0 }

    UstadNumberTextEditField {
        value = aNumber
        label = ReactNode("Phone")
        placeholder = "Phone"
        disabled = false
        endAdornment = ReactNode("points")
        inputProps = jso {
            readOnly = true
            inputMode = InputMode.numeric
        }

        onChange = {
            aNumber = it
        }
    }
}