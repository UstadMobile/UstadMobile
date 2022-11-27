package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.mui.common.*
import kotlinx.js.jso
import mui.icons.material.Visibility
import mui.icons.material.VisibilityOff
import mui.material.*
import muix.pickers.*
import react.*
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.onChange
import kotlin.js.Date

external interface UstadEditFieldProps: PropsWithChildren {

    /**
     * Text value in the field
     */
    var value: String?

    /**
     * Label to show the user
     */
    var label: String?

    /**
     * Error text, if any. Default is null. Null indicates no error. If there is an error, the field
     * will be in error state (e.g. red) and the text will be shown underneath
     */
    var error: String?

    /**
     * Enabled or disabled
     */
    var enabled: Boolean?

    /**
     * onChange event handler
     */
    var onChange: (String) -> Unit

    /**
     * True if this is a password field, false otherwise (default). If this is a password field, then
     * the field content will be hidden by default. A button will be added to the end of the field
     * to allow the user to toggle visibility.
     */
    var password: Boolean

    /**
     * Optional onClick handler. This is generally used for fields that actually lead to a picker
     * of some kind.
     */
    var onClick: (() -> Unit)?

    /**
     * Sets the readonly attribute on the underlying Input element. Roughly as per
     *  https://codesandbox.io/s/rect-material-ui-textfield-readonly-st5of?from-embed=&file=/src/index.js:232-249
     */
    var readOnly: Boolean
}

/**
 * Base TextEditField handler. Will show an error message below the field if the error prop is any
 * non null value. The error will be automatically cleared when the user updates the field.
 */
val UstadTextEditField = FC<UstadEditFieldProps> { props ->
    var errorText by useState { props.error }

    var passwordVisible by useState { false }

    val strings = useStringsXml()

    TextField {
        label = ReactNode(props.label ?: "")
        value = props.value
        disabled = !(props.enabled ?: true)
        error = errorText != null
        helperText = errorText?.let { ReactNode(it) }
        if(props.readOnly) {
            inputProps = jso {
                readOnly = true
            }
        }

        onClick = props.onClick?.let { onClickHandler ->
            {
                onClickHandler()
            }
        }

        onChange = {
            val currentVal = it.target.asDynamic().value
            errorText = null
            props.onChange(currentVal?.toString() ?: "")
        }

        if(props.password) {
            type = if(passwordVisible) {
                InputType.text
            }else {
                InputType.password
            }

            //As per MUI showcase
            asDynamic().InputProps = jso<InputBaseProps> {
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    IconButton {
                        ariaLabel = strings[MessageID.toggle_visibility]
                        onClick = {
                            passwordVisible = !passwordVisible
                        }

                        if(passwordVisible) {
                            VisibilityOff { }
                        }else {
                            Visibility { }
                        }
                    }
                }
            }
        }
    }
}

external interface UstadDateEditFieldProps : Props {

    /**
     * The value as time in millis since 1970
     */
    var timeInMillis: Long

    /**
     * Field label
     */
    var label: String

    /**
     * onChange function. Will provide the selected time in milliseconds since 1970
     */
    var onChange: (Long) -> Unit

    var error: String?

    var enabled: Boolean?

}

/**
 * Max value for a Javascript date as per
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
 */
val JS_DATE_MAX = 8640000000000000L

fun Long.asDate(): Date? {
    val value = if(this > JS_DATE_MAX) JS_DATE_MAX else this

    return if(this == 0L || this >= JS_DATE_MAX) {
        null
    }else {
        Date(value)
    }
}


val UstadDateEditField = FC<UstadDateEditFieldProps> { props ->

    LocalizationProvider {
        dateAdapter = AdapterDateFns

        MobileDatePicker {
            disabled = !(props.enabled ?: true)
            label = ReactNode(props.label)
            value = props.timeInMillis.asDate()

            onChange = {
                props.onChange(it.getTime().toLong())
            }

            renderInput = { params ->
                TextField.create {
                    +params

                    if(props.error != null) {
                        error = true
                        helperText = props.error?.let { ReactNode(it) }
                    }
                }
            }
        }
    }
}


external interface MessageIDDropDownFieldProps: Props {
    /**
     * The currently selected value. If there is no such value in the list, the selection will be blank
     */
    var value: Int

    /**
     * A list of options to show.
     * @see MessageIdOption2
     */
    var options: List<MessageIdOption2>

    /**
     * Field label
     */
    var label: String

    /**
     * Event handler
     */
    var onChange: (MessageIdOption2?) -> Unit

    /**
     * DOM element id
     */
    var id: String?

    /**
     *
     */
    var enabled: Boolean?

    /**
     *
     */
    var error: String?
}

val UstadMessageIdDropDownField = FC<MessageIDDropDownFieldProps> { props ->
    val strings = useStringsXml()

    FormControl {
        fullWidth = true

        InputLabel {
            id = "${props.id}_label"
            +props.label
        }

        Select {
            value = props.value
            id = props.id
            labelId = "${props.id}_label"
            label = ReactNode(props.label)
            disabled = !(props.enabled ?: true)
            onChange = { event, _ ->
                val selectedVal = ("" + event.target.value).toInt()
                val selectedItem = props.options.firstOrNull { it.value ==  selectedVal }
                props.onChange(selectedItem)
            }

            props.options.forEach { option ->
                MenuItem {
                    value = option.value
                    +strings[option.messageId]
                }
            }
        }

        val helperText = props.error
        if(helperText != null) {
            FormHelperText {
                +helperText
            }
        }
    }
}

val UstadEditFieldPreviews = FC<Props> {
    Stack {
        UstadDateEditField {
            timeInMillis = systemTimeInMillis()
            label = "Date"
            onChange = { }
            error = "Bad Day"
            enabled = false
        }

        UstadTextEditField {
            label = "Read only field"
            value = "Cant change me"
            onChange = { }
            readOnly = true
            onClick = {
                println("Read only field clicked")
            }
        }
    }
}




