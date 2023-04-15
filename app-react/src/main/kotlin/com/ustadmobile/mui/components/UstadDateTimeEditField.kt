package com.ustadmobile.mui.components

import com.ustadmobile.core.util.ext.chopOffSeconds
import com.ustadmobile.door.util.systemTimeInMillis
import csstype.px
import js.core.jso
import kotlinx.datetime.*
import mui.material.TextField
import mui.system.PropsWithSx
import mui.system.sx
import react.*
import react.dom.onChange
import web.html.HTMLInputElement
import web.html.InputType

/**
 * Date Time input field based on using timeInMillis and a timezone. This uses the native
 * datetime input format because DateTimePicker on MUI 5 doesn't work as expected (this should
 * be fixed in MUI6).
 */
external interface UstadDateTimeEditFieldProps: PropsWithSx {
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

    var fullWidth: Boolean?

    /**
     * We often use 0 and Long.MAX_VALUE as a placeholder for a default (e.g. unset) date. If this
     * property is set, then this value will be emitted by the onChange function when the user deletes
     * the date
     */
    var unsetDefault: Long?

}

val UstadDateTimeEditField = FC<UstadDateTimeEditFieldProps> { props ->

    fun Long.timeToIsoDateTimeInputFieldString(): String {
        return if(this != (props.unsetDefault ?: 0L)) {
            Instant.fromEpochMilliseconds(this)
                .toLocalDateTime(TimeZone.of(props.timeZoneId))
                .chopOffSeconds()
                .toString()
        }else {
            ""
        }
    }

    if(props.timeInMillis != props.unsetDefault && props.timeInMillis > JS_DATE_MAX) {
        throw IllegalArgumentException("UstadDateTimeEditField: Date to display is out of allowed " +
            "range. If Long.MAX_VALUE is being used as a fallback default, set the unsetDefault " +
            "property e.g. unsetDefault = Long.MAX_VALUE")
    }

    var rawValue: String by useState {
        props.timeInMillis.timeToIsoDateTimeInputFieldString()
    }

    useEffect(props.timeInMillis){
        rawValue = props.timeInMillis.timeToIsoDateTimeInputFieldString()
    }

    TextField {
        type = InputType.datetimeLocal
        label = ReactNode(props.label)
        value = rawValue
        InputLabelProps = jso {
            shrink = true
        }
        id = props.id
        fullWidth = props.fullWidth
        sx = props.sx
        onChange = {
            val targetElValue = (it.target as HTMLInputElement).value
            if(targetElValue.isNotBlank()) {
                val localDateTime = LocalDateTime.parse(targetElValue)
                val instant = localDateTime.toInstant(TimeZone.of(props.timeZoneId))
                props.onChange(instant.toEpochMilliseconds())
            }else {
                props.onChange(props.unsetDefault ?: 0L)
            }
        }

    }
}

val DateTimeEditFieldPreview = FC<Props> {
    var dateTime: Long by useState { 0 }
    UstadDateTimeEditField {
        sx {
            margin = 20.px
        }
        timeInMillis = dateTime
        timeZoneId = TimeZone.currentSystemDefault().id
        label = "Date and time"
        onChange = {
            dateTime = it
        }
        enabled = true
    }

}

