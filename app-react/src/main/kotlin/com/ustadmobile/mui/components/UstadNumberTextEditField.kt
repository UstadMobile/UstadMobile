package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringsXml
import js.core.jso
import mui.icons.material.*
import mui.material.*
import react.*
import react.dom.onChange

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
    var trailingIcon: ReactNode?

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

    val strings = useStringsXml()

    TextField {
        label = props.label
        value = props.value
        disabled = !(props.enabled ?: true)

        //As per MUI showcase
        asDynamic().InputProps = jso<InputBaseProps> {
            endAdornment = props.trailingIcon
        }
//        error = errorText != null
//        helperText = errorText?.let { ReactNode(it) }
//        fullWidth = props.fullWidth

        onChange = {
//            val currentVal = it.target.asDynamic().value
//            errorText = null
//            props.onChange(currentVal?.toString() ?: "")
        }

    }

}

val UstadNumberTextEditFieldPreview = FC<Props> {

    var aNumber by useState { 0 }

    UstadNumberTextEditField {
        value = aNumber
        label = ReactNode("Phone Number")
        enabled = true
        trailingIcon = InputAdornment.create {
            position = InputAdornmentPosition.end
            Icon {
                Numbers
            }
        }
        onChange = {
            aNumber = it
        }
//        trailingIcon = {
//            Text("points")
//        }
    }
}