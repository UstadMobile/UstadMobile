package com.ustadmobile.mui.common

import mui.material.*
import muix.pickers.MobileDatePickerProps
import react.ReactNode
import kotlin.js.Date

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

inline var MobileDatePickerProps.label: ReactNode?
    get() = asDynamic().label
    set(value) {
        asDynamic().label = value
    }

inline var MobileDatePickerProps.renderInput: (BaseTextFieldProps) -> ReactNode
    get() = asDynamic().renderInput
    set(noinline value) {
        asDynamic().renderInput = value
    }

inline var MobileDatePickerProps.value: Date?
    get() = asDynamic().value
    set(value) {
        asDynamic().value = value
    }

inline var MobileDatePickerProps.onChange: (Date) -> Unit
    get() = asDynamic().onChange
    set(noinline value) {
        asDynamic().onChange = value
    }
