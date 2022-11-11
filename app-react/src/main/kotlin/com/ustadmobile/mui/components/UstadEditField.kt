package com.ustadmobile.mui.components

import mui.material.*
import muix.pickers.*
import react.*
import react.dom.onChange
import kotlin.js.Date

external interface UstadEditFieldProps: PropsWithChildren {

    var value: String?

    var label: String?

    var error: String?

    var enabled: Boolean?

    var onChange: (String) -> Unit
}

val UstadTextEditField = FC<UstadEditFieldProps> { props ->
    TextField {
        label = ReactNode(props.label ?: "")
        value = props.value
        disabled = !(props.enabled ?: true)
        helperText = props.error?.let { ReactNode(it) }
        onChange = {
            val currentVal = it.target.asDynamic().value
            props.onChange(currentVal?.toString() ?: "")
        }
    }
}

val UstadDateEditField = FC<Props> {

    LocalizationProvider {
        dateAdapter = AdapterDateFns
        MobileDatePicker {
            asDynamic().label = ReactNode("Date")
            asDynamic().date = Date()

            asDynamic().onChange = { it: Date ->
                println(it.getTime())
            }

            asDynamic().renderInput = { it: BaseTextFieldProps ->
                TextField.create {

                    onClick = it.onClick
                    value = it.value
                    label = it.label
                }
            }
        }
    }


}

external interface UstadExposedDropDownMenuFieldProps<T> : Props{

    var value: T?

    var options: List<T>

    var label: String

    var id: String?

    var itemText: (T) -> String

    var itemValue: (T) -> String

}

val UstadExposedDropDownMenuField = FC<UstadExposedDropDownMenuFieldProps<Any>> { props ->
    FormControl {

        InputLabel {
            id = "${props.id}_label"
            +props.label
        }

        Select {
            value = props.value
            id = props.id
            labelId = "${props.id}_label"

            props.options.forEach { option ->
                MenuItem {
                    value = props.itemValue(option)
                    +props.itemText(option)
                }
            }
        }
    }
}



