package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.*
import mui.material.TextField
import muix.pickers.AdapterDateFns
import muix.pickers.LocalizationProvider
import muix.pickers.TimePicker
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadTimeEditFieldProps: Props {
    /**
     * The value as time in millis since midnight
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

val UstadTimeEditField = FC<UstadTimeEditFieldProps> { props ->
    LocalizationProvider {
        dateAdapter = AdapterDateFns

        TimePicker {
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
