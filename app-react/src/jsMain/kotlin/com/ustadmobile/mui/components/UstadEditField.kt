package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.mui.common.*
import com.ustadmobile.view.components.UstadSwitchField
import js.objects.jso
import kotlinx.datetime.*
import mui.icons.material.Visibility
import mui.icons.material.VisibilityOff
import mui.material.*
import mui.system.responsive
import react.*
import react.dom.aria.ariaLabel
import web.html.InputMode
import web.html.InputType
import react.dom.onChange

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

    /**
     * Sets a suffix string at the end e.g. a unit of measurement e.g. "points", "%", etc. Displayed
     * at the end of the TextField as an adornment.
     */
    var suffixText: String?

    /**
     * InputProps setter functions - can be used to add adornments, set the input type, etc.
     */
    var inputProps: ((InputBaseProps) -> Unit)?

    /**
     * Fullwidth property: passed through to the TextField
     */
    var fullWidth: Boolean

    /**
     * DOM ID
     */
    var id: String?
}

/**
 * Base TextEditField handler. Will show an error message below the field if the error prop is any
 * non null value. The error will be automatically cleared when the user updates the field.
 */
val UstadTextEditField = FC<UstadEditFieldProps> { props ->
    var errorText by useState { props.error }

    var passwordVisible by useState { false }

    val strings = useStringProvider()

    TextField {
        label = ReactNode(props.label ?: "")
        value = props.value
        disabled = !(props.enabled ?: true)
        error = errorText != null
        helperText = errorText?.let { ReactNode(it) }
        fullWidth = props.fullWidth
        id = props.id

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
            type = if (passwordVisible) {
                InputType.text
            } else {
                InputType.password
            }
        }

        //As per MUI showcase
        asDynamic().InputProps = jso<InputBaseProps> {
            if(props.password) {
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    IconButton {
                        ariaLabel = strings[MR.strings.toggle_visibility]
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
            }else if(props.suffixText != null) {
                endAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.end
                    +(props.suffixText ?: "")
                }
            }

            props.inputProps?.also { inputPropsFn ->
                inputPropsFn(this)
            }
        }
    }
}



external interface UstadDropDownFieldProps: Props {
    /**
     * The currently selected value. If there is no such value in the list, the selection will be blank
     */
    var value: Any?

    /**
     * A list of options to show.
     */
    var options: List<Any>

    /**
     * A function that will generate a ReactNode to show in the dropdown for an item that is in the
     * list of options. Normally this would just be ReactNode with plain text, but it could also
     * include images or other nodes.
     *
     * e.g. itemLabel = {
     *   ReactNode((it as MyItemType).name)
     * }
     */
    var itemLabel: (Any) -> ReactNode

    /**
     * A function that will generate a unique string for any item in the list of options. This string
     * will be used for the value tag in the select menuItem.
     *
     * e.g. itemValue = {
     *    (it as MyItemType).id
     * }
     */
    var itemValue: (Any) -> String

    /**
     * Field label
     */
    var label: String

    /**
     * Event handler
     */
    var onChange: (Any?) -> Unit

    /**
     * DOM element id
     */
    var id: String?

    /**
     * Enabled / disabled control
     */
    var enabled: Boolean?

    /**
     * An error message to show the user. If non-null, the component will be shown in an error state
     * and the error message will be shown below.
     */
    var error: String?

    var fullWidth: Boolean
}


val UstadDropDownField = FC<UstadDropDownFieldProps> { props ->
    FormControl {
        fullWidth = true

        InputLabel {
            id = "${props.id}_label"
            +props.label
        }

        Select {
            value = props.value?.let { props.itemValue(it) }
            id = props.id
            labelId = "${props.id}_label"
            label = ReactNode(props.label)
            disabled = !(props.enabled ?: true)
            fullWidth = props.fullWidth
            onChange = { event, _ ->
                val selectedVal = ("" + event.target.value)
                val selectedItem = props.options.firstOrNull { props.itemValue(it) ==  selectedVal }
                props.onChange(selectedItem)
            }

            props.options.forEach { option ->
                MenuItem {
                    value = props.itemValue(option)
                    + props.itemLabel(option)
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


class DropDownOption(val label: String, val value: String) {
    override fun toString(): String {
        return "DropDownOption label=$label value=$value"
    }
}

val UstadEditFieldPreviews = FC<Props> {
    Stack {
        spacing = responsive(5)

        var date1 : Long by useState { systemTimeInMillis() }
        UstadDateField {
            timeInMillis = date1
            id = "date_edit_field"
            timeZoneId = TimeZone.currentSystemDefault().id
            label = ReactNode("Date")
            onChange = {
                date1 = it
            }
            error = true
            helperText = ReactNode("Bady Day")
        }

        var unsetMinDate: Long by useState { 0L }

        UstadDateField {
            timeInMillis = unsetMinDate
            timeZoneId = TimeZone.currentSystemDefault().id
            id = "date_edit_unset"
            label = ReactNode("Unset min date")
            onChange = {
                unsetMinDate = it
            }
        }


        var time: Int by useState { (14 * MS_PER_HOUR) + (30 * MS_PER_MIN) }

        UstadTimeField {
            timeInMillis = time
            id = "time_field"
            label = ReactNode("Time")
            onChange = {
                time = it
            }
        }

        UstadTextEditField {
            label = "Read only field"
            value = "Cant change me"
            id = "read_only_edit_field"
            onChange = { }
            readOnly = true
            onClick = {
                println("Read only field clicked")
            }
        }

        var selectedOption: DropDownOption? by useState { DropDownOption("One", "1") }

        UstadDropDownField {
            value = selectedOption
            label = "Select options"
            id = "select_field"
            options = listOf(DropDownOption("One", "1"),
                DropDownOption("Two", "2"))
            itemLabel = { ReactNode((it as? DropDownOption)?.label ?: "") }
            itemValue = { (it as? DropDownOption)?.value ?: "" }
            onChange = {
                selectedOption = it as? DropDownOption
            }
        }

        var switchChecked by useState { false }

        UstadSwitchField {
            label = "Switch"
            id = "switch_field"
            checked = switchChecked
            onChanged = {
                switchChecked = it
            }
        }

        var maxScore by useState { 42 }
        UstadTextEditField {
            label = "Maximum score"
            id = "maximum_score"
            value = maxScore.toString()
            onChange = { newString ->
                maxScore = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            inputProps = {
                it.inputMode = InputMode.numeric
            }
            suffixText = "Points"
        }

    }
}




