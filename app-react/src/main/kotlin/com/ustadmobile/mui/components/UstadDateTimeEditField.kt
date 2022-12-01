package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.*
import mui.material.TextField
import muix.pickers.AdapterDateFns
import muix.pickers.DateTimePicker
import muix.pickers.LocalizationProvider
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UstadDateTimeEditFieldProps: Props {
    /**
     * The value as time in millis since 1970
     */
    var timeInMillis: Long

    /**
     * Reserved for future usage: will be required
     */
    @Suppress("unused")
    var timeZoneId: String

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

val UstadDateTimeEditField = FC<UstadDateTimeEditFieldProps> { props ->
    LocalizationProvider {
        dateAdapter = AdapterDateFns

        DateTimePicker {
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

