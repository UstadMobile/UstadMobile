package com.ustadmobile.mui.components

import js.core.jso
import mui.material.*
import react.*
import react.dom.onChange
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

    /**
     * Label to show the user
     */
    var placeholder: String?

    /**
     * Label to show the user
     */
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
    var enabled: Boolean?

    /**
     * onChange event handler
     */
    var onChange: (Int) -> Unit

    /**
     * InputProps setter functions - can be used to add adornments, set the input type, etc.
     */
    var inputProps: ((InputBaseProps) -> Unit)?

}

val UstadNumberTextEditField = FC<UstadNumberTextEditFieldProps> { props ->

    var errorText by useState { props.error }

    var rawValue by useState {
        if(props.value != 0) props.value.toString() else ""
    }

    TextField {
        label = props.label
        value = rawValue
        disabled = !(props.enabled ?: true)
        placeholder = props.placeholder

        //As per MUI showcase
        asDynamic().InputProps = jso<InputBaseProps> {
            endAdornment = props.endAdornment

            props.inputProps?.also { inputPropsFn ->
                inputPropsFn(this)
            }
        }

        error = errorText != null
        helperText = errorText?.let { ReactNode(it) }
        fullWidth = props.fullWidth
        type = InputType.number
        onChange = {
            val text = (it.target.asDynamic().value)?.toString() ?: ""
            errorText = null

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
        label = ReactNode("Phone Number")
        enabled = true
        endAdornment = InputAdornment.create {
            Typography {
               + "points"
            }
        }
        onChange = {
            aNumber = it
        }
    }
}