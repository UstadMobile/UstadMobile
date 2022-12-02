package com.ustadmobile.mui.components

import com.ustadmobile.hooks.useTimeOfDayInMsAsJsDate
import com.ustadmobile.mui.common.*
import com.ustadmobile.util.ext.toTimeOfDayInMs
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
     * The value of the time of day as milliseconds since midnight
     */
    var timeInMillis: Int

    /**
     * Field label
     */
    var label: String

    /**
     * onChange function. Will provide the selected time as milliseconds since midnight
     */
    var onChange: (Int) -> Unit

    var error: String?

    var enabled: Boolean?

}

val UstadTimeEditField = FC<UstadTimeEditFieldProps> { props ->
    val jsDateVal = useTimeOfDayInMsAsJsDate(props.timeInMillis)

    LocalizationProvider {
        dateAdapter = AdapterDateFns

        TimePicker {
            disabled = !(props.enabled ?: true)
            label = ReactNode(props.label)
            value = jsDateVal

            onChange = {
                props.onChange(it.toTimeOfDayInMs())
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
