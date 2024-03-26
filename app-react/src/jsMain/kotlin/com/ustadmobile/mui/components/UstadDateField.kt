package com.ustadmobile.mui.components

import js.objects.jso
import kotlinx.datetime.*
import mui.material.TextField
import react.*
import react.dom.onChange
import web.html.HTMLInputElement
import web.html.InputType


external interface UstadDateFieldProps : Props {

    /**
     * The value as time in millis since 1970
     */
    var timeInMillis: Long

    /**
     * Required: The timezone being used
     */
    var timeZoneId: String

    /**
     * Field label
     */
    var label: ReactNode?

    var helperText: ReactNode?

    /**
     * onChange function. Will provide the selected time in milliseconds since 1970
     */
    var onChange: (Long) -> Unit

    var error: Boolean?

    var disabled: Boolean?

    var fullWidth: Boolean?

    /**
     * We often use 0 and Long.MAX_VALUE as a placeholder for a default (e.g. unset) date. If this
     * property is set, then this value will be emitted by the onChange function when the user deletes
     * the date
     */
    var unsetDefault: Long?

    var id: String?

}

/**
 * Max value for a Javascript date as per
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
 */
val JS_DATE_MAX = 8640000000000000L

/**
 * We often use 0 and Long.MAX_VALUE as placeholders for unset dates. This makes queries
 * straightforward e.g. if no end date is set by the user, the end date is stored as Long.MAX_VALUE,
 * and would appear in active courses if applicable etc.
 *
 * These unset dates should not (however) be displayed to the user.
 */
fun Long.isSetDate(): Boolean {
    return this > 0L && this < JS_DATE_MAX
}

val UstadDateField = FC<UstadDateFieldProps> { props ->

    fun Long.timeToIsoDateInputFieldString(): String {
        return if(this != (props.unsetDefault ?: 0)) {
            Instant.fromEpochMilliseconds(this)
                .toLocalDateTime(TimeZone.of(props.timeZoneId))
                .date.toString()
        }else {
            ""
        }
    }

    var rawValue: String? by useState {
        props.timeInMillis.timeToIsoDateInputFieldString()
    }

    useEffect(props.timeInMillis) {
        val strVal = props.timeInMillis.timeToIsoDateInputFieldString()
        if(rawValue != strVal)
            rawValue = strVal
    }


    TextField {
        type = InputType.date
        label = props.label
        value = rawValue
        InputLabelProps = jso {
            shrink = true
        }
        id = props.id
        fullWidth = props.fullWidth
        helperText = props.helperText
        error = props.error
        disabled = props.disabled

        onChange = {
            val targetElValue = (it.target as HTMLInputElement).value
            rawValue = targetElValue
            if(targetElValue.isNotEmpty()) {
                val instant = LocalDateTime(LocalDate.parse(targetElValue),
                    LocalTime(0, 0 ))
                    .toInstant(TimeZone.of(props.timeZoneId))
                console.log("onChange: $targetElValue")
                props.onChange(instant.toEpochMilliseconds())
            }else {
                console.log("onChange: unset")
                props.onChange(props.unsetDefault ?: 0L)
            }
        }
    }
}

