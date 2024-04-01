package com.ustadmobile.mui.common

import web.cssom.Cursor
import web.cssom.JustifyContent
import csstype.PropertiesBuilder
import js.objects.jso
import mui.material.*
import web.cssom.Display

//As per MissedWrappers on MUI-Showcase
inline var GridProps.xs: Int
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().xs = value
    }
@Suppress("unused")
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

inline var TabsProps.ariaLabel: String
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic()["aria-label"] = value
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
        asDynamic()["-webkit-line-clamp"] = value.toString()
    }

val DisplayWebkitBox = "-webkit-box".unsafeCast<Display>()

inline var PropertiesBuilder.webkitBoxOrient: String
    get() = asDynamic()["-webkit-box-orient"]
    set(value) {
        asDynamic()["-webkit-box-orient"] = value
    }

