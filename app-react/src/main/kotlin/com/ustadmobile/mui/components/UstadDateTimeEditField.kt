package com.ustadmobile.mui.components

import com.ustadmobile.hooks.useTimeInOtherTimeZoneAsJsDate
import com.ustadmobile.mui.common.*
import com.ustadmobile.util.ext.toMillisInOtherTimeZone
import mui.material.TextField
import muix.pickers.AdapterDateFns
import muix.pickers.DateTimePicker
import muix.pickers.LocalizationProvider
import react.*

external interface UstadDateTimeEditFieldProps: Props {
    /**
     * The value as time in millis since 1970
     */
    var timeInMillis: Long

    /**
     * The TimeZone ID to use for this field. E.g the timezone for the course, school, etc. This is
     * mandatory. If you want to use the system default Timezone, you can use kotlinx datetime
     * TimeZone.currentSystemDefault().id
     */
    var timeZoneId: String

    /**
     * Field label
     */
    var label: String

    /**
     * onChange function. Will provide the selected time in milliseconds since 1970
     */
    var onChange: (Long) -> Unit

    /**
     * Error text to show, if any
     */
    var error: String?

    /**
     * Set to false to disable. Default (null) will set the component as enabled.
     */
    var enabled: Boolean?

    var id: String?

}

val UstadDateTimeEditField = FC<UstadDateTimeEditFieldProps> { props ->
    val jsDateVal = useTimeInOtherTimeZoneAsJsDate(props.timeInMillis,
        props.timeZoneId)

    LocalizationProvider {
        dateAdapter = AdapterDateFns

        DateTimePicker {
            disabled = !(props.enabled ?: true)
            label = ReactNode(props.label)
            value = jsDateVal

            onChange = {
                props.onChange(it.toMillisInOtherTimeZone(props.timeZoneId))
            }

            renderInput = { params ->
                TextField.create {
                    +params

                    id = props.id

                    if(props.error != null) {
                        error = true
                        helperText = props.error?.let { ReactNode(it) }
                    }
                }
            }
        }
    }
}

