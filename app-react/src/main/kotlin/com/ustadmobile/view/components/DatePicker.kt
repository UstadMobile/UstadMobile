package com.ustadmobile.view.components

import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.setStyledPropsAndRunHandler
import com.ustadmobile.util.moment
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*
import styled.Styled
import styled.StyledHandler
import styled.StyledProps
import kotlin.js.Date

@JsModule("react-multi-date-picker")
@JsNonModule
private external val multiDatePicker: dynamic

@JsModule("react-multi-date-picker/plugins/time_picker")
@JsNonModule
private external val multiTimePicker: dynamic

@Suppress("UnsafeCastFromDynamic")
private val multiDatePickerComponent: RComponent<MultiDatePickerProps, RState> = multiDatePicker.default


@Suppress("UnsafeCastFromDynamic")
private val multiTimePickerComponent: RComponent<MultiTimePickerProps, RState> = multiTimePicker.default

enum class MDatePickerPosition(value: String){
    bottom("bottom"),
    top("top"),
    right("right"),
    left("left")
}

external interface MultiTimePickerProps: StyledProps {
    var position:MDatePickerPosition
    var hideSeconds: Boolean
    var calendarProps: dynamic
    var currentDate: dynamic
    var selectedDate: dynamic
    var date: dynamic
    var state: dynamic
}

fun RBuilder.mTimePicker(position: MDatePickerPosition = MDatePickerPosition.bottom,
                         currentDate: dynamic,
                         calendarProps: dynamic = Any(),
                         selectedDate: dynamic = Any(),
                         hideSeconds: Boolean = false,
                         className: String? = null,
                         handler: StyledHandler<MultiTimePickerProps>) = createStyled(
    multiTimePickerComponent){
    attrs.position = position
    attrs.calendarProps = calendarProps
    attrs.className = className
    attrs.hideSeconds = hideSeconds
    attrs.currentDate = currentDate
    attrs.selectedDate = selectedDate
    attrs.date = currentDate
    attrs.state = attrs
    attrs.state.date = currentDate
    setStyledPropsAndRunHandler(className, handler)

}

external interface MultiDatePickerProps: StyledProps {
    var value: Any
    var format: String
    var range: Boolean
    var currentDate: dynamic
    var multiple: Boolean
    var plugins: Array<Any>
    var disableDayPicker: Boolean
    var formattingIgnoreList: Array<Any>?
    var render: ((Any, () -> Unit) -> ReactElement)?
    var onChange: ((Event) -> Unit)?
}


fun RBuilder.mDatePicker(format: String,
                         value: Long = Date().getTime().toLong(),
                         currentDate: Date = Date(value),
                         multiple: Boolean = false,
                         range: Boolean = false,
                         disableDayPicker: Boolean = false,
                         plugins: Array<Any>? = null,
                         formattingIgnoreList: Array<Any> = arrayOf("HH:mm:ss"),
                         onChange: ((Event) -> Unit)? = null,
                         render: ((Any, () -> Unit) -> ReactElement)? = null, className: String? = null,
                         handler: StyledHandler<MultiDatePickerProps>) = createStyled(multiDatePickerComponent) {
    attrs.value = value
    attrs.format = format
    attrs.range = range
    attrs.render = render
    attrs.multiple = multiple
    attrs.className = className
    attrs.currentDate = multiDatePicker.toDateObject(currentDate)
    attrs.disableDayPicker = disableDayPicker
    attrs.formattingIgnoreList = formattingIgnoreList
    attrs.onChange = onChange
    val dateProps = attrs
    if(plugins != null){
        attrs.plugins = plugins
    } else{
        attrs.plugins = arrayOf(mTimePicker(currentDate = attrs.currentDate) {
            attrs.calendarProps = dateProps
        })
    }
    setStyledPropsAndRunHandler(className, handler)
}