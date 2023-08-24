package com.ustadmobile.mui.common

import web.cssom.Cursor
import web.cssom.JustifyContent
import csstype.PropertiesBuilder
import js.core.jso
import mui.material.*
import muix.pickers.*
import react.ReactNode
import kotlin.js.Date
import web.cssom.Display

//As per MissedWrappers on MUI-Showcase
// TODO: Remove when it will be implemented in MUI wrappers
inline var GridProps.xs: Int
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().xs = value
    }

inline var GridProps.sm: Int
    get() = TODO("Prop is write only!")
    set(value) {
        asDynamic().sm = value
    }

inline var GridProps.md: Int
    get() = TODO()
    set(value) {
        asDynamic().md = value
    }


inline var GridProps.lg: Int
    get() = TODO()
    set(value) {
        asDynamic().lg = value
    }

inline var TypographyProps.color: String
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().color = value
    }

inline var BoxProps.noValidate: Boolean
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().noValidate = value
    }

inline var BoxProps.autoComplete: String
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().autoComplete = value
    }

inline var TabsProps.ariaLabel: String
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic()["aria-label"] = value
    }

inline var DatePickerProps.label: ReactNode?
    get() = asDynamic().label
    set(value) {
        asDynamic().label = value
    }

inline var DatePickerProps.renderInput: (BaseTextFieldProps) -> ReactNode
    get() = asDynamic().renderInput
    set(noinline value) {
        asDynamic().renderInput = value
    }

inline var DatePickerProps.value: Date?
    get() = asDynamic().value
    set(value) {
        asDynamic().value = value
    }

inline var DatePickerProps.onChange: (Date) -> Unit
    get() = asDynamic().onChange
    set(noinline value) {
        asDynamic().onChange = value
    }


inline var DatePickerProps.disabled: Boolean
    get() = asDynamic().disabled
    set(value) {
        asDynamic().disabled = value
    }

inline var DateTimePickerProps.label: ReactNode?
    get() = asDynamic().label
    set(value) {
        asDynamic().label = value
    }

inline var DateTimePickerProps.renderInput: (BaseTextFieldProps) -> ReactNode
    get() = asDynamic().renderInput
    set(noinline value) {
        asDynamic().renderInput = value
    }

inline var DateTimePickerProps.value: Date?
    get() = asDynamic().value
    set(value) {
        asDynamic().value = value
    }

inline var DateTimePickerProps.onChange: (Date) -> Unit
    get() = asDynamic().onChange
    set(noinline value) {
        asDynamic().onChange = value
    }


inline var DateTimePickerProps.disabled: Boolean
    get() = asDynamic().disabled
    set(value) {
        asDynamic().disabled = value
    }


inline var TimePickerProps.label: ReactNode?
    get() = asDynamic().label
    set(value) {
        asDynamic().label = value
    }

inline var TimePickerProps.renderInput: (BaseTextFieldProps) -> ReactNode
    get() = asDynamic().renderInput
    set(noinline value) {
        asDynamic().renderInput = value
    }

inline var TimePickerProps.value: Date?
    get() = asDynamic().value
    set(value) {
        asDynamic().value = value
    }

inline var TimePickerProps.onChange: (Date) -> Unit
    get() = asDynamic().onChange
    set(noinline value) {
        asDynamic().onChange = value
    }


inline var TimePickerProps.disabled: Boolean
    get() = asDynamic().disabled
    set(value) {
        asDynamic().disabled = value
    }


inline var InputBaseComponentProps.readOnly: Boolean
    get() = asDynamic().readOnly
    set(value) {
        asDynamic().readOnly = value
    }

inline var StackProps.justifyContent: JustifyContent
    get() = asDynamic().justifyContent
    set(value) {
        asDynamic().justifyContent = value
    }

inline fun PropertiesBuilder.input(
    crossinline block: PropertiesBuilder.() -> Unit,
) {
    asDynamic().input = jso(block)
}

inline var PropertiesBuilder.inputCursor: Cursor?
    get() = asDynamic().input?.cursor
    set(value) {
        if(asDynamic().input != null) {
            asDynamic().input.cursor = value
        }else {
            asDynamic().input = jso<PropertiesBuilder> {
                cursor = value
            }
        }
    }


inline var PropertiesBuilder.webKitLineClamp: Int
    get() = asDynamic()["-webkit-line-clamp"]
    set(value) {
        asDynamic()["-webkit-line-clamp"] = value
    }

val DisplayWebkitBox = "-webkit-box".unsafeCast<Display>()

inline var PropertiesBuilder.webkitBoxOrient: String
    get() = asDynamic()["-webkit-box-orient"]
    set(value) {
        asDynamic()["-webkit-box-orient"] = value
    }

