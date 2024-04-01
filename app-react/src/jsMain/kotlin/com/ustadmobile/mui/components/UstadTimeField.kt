package com.ustadmobile.mui.components

import com.ustadmobile.core.util.ext.chopOffSeconds
import js.objects.jso
import kotlinx.datetime.LocalTime
import mui.material.TextField
import mui.system.PropsWithSx
import react.*
import react.dom.onChange
import web.html.HTMLInputElement
import web.html.InputType

external interface UstadTimeFieldProps: PropsWithSx {
    /**
     * The value of the time of day as milliseconds since midnight
     */
    var timeInMillis: Int

    /**
     * Field label
     */
    var label: ReactNode?

    var helperText: ReactNode?

    var error: Boolean?

    /**
     * onChange function. Will provide the selected time as milliseconds since midnight
     */
    var onChange: (Int) -> Unit

    var disabled: Boolean?

    var fullWidth: Boolean

    var unsetDefault: Int?

    var id: String?

}

val UstadTimeField = FC<UstadTimeFieldProps> { props ->

    fun Int.toTimeOfDayInputFieldString() : String{
        return if(this != (props.unsetDefault ?: 0)) {
            return LocalTime.fromMillisecondOfDay(this)
                .chopOffSeconds()
                .toString()
        }else {
            ""
        }
    }

    var rawValue: String by useState { props.timeInMillis.toTimeOfDayInputFieldString() }

    useEffect(props.timeInMillis) {
        val strValue = props.timeInMillis.toTimeOfDayInputFieldString()
        if(rawValue != strValue) {
            rawValue = strValue
        }
    }

    TextField {
        type = InputType.time
        value = rawValue
        label = props.label
        disabled = props.disabled
        helperText = props.helperText
        fullWidth = props.fullWidth
        sx = props.sx
        error = props.error

        onChange = {
            val targetElValue = (it.target as HTMLInputElement).value
            if(targetElValue.isNotBlank()) {
                val time = LocalTime.parse(targetElValue)
                console.log("onChange time = $time ${time.toMillisecondOfDay()}")
                props.onChange(time.toMillisecondOfDay())
            }else {
                console.log("onChange time = 0")
                props.onChange(props.unsetDefault ?: 0)
            }
        }
        InputLabelProps = jso {
            shrink = true
        }
    }
}
