package com.ustadmobile.mui.theme

import Breakpoints
import csstype.ZIndex
import kotlinext.js.jsObject
import react.Props

external interface ShapeOptions {
    var borderRadius: Int
}

external interface Spacing

external interface ThemeOptions {
    var breakpoints: Breakpoints
    var direction: dynamic /* String /* "ltr" */ | String /* "rtl" */ */ get() = definedExternally; set(value) = definedExternally
    var mixins: dynamic
    var overrides: dynamic
    var palette: PaletteOptions? get() = definedExternally; set(value) = definedExternally
    var props: Props
    var shadows: dynamic
    var spacing: dynamic
    var typography: TypographyOptions? get() = definedExternally; set(value) = definedExternally
    var zIndex: dynamic
}

external interface Theme {
    var shape: ShapeOptions
    var breakpoints: Breakpoints
    var direction: dynamic /* String /* "ltr" */ | String /* "rtl" */ */
    var mixins: Mixins
    var overrides: dynamic
    var palette: Palette
    var props: Props
    var shadows: dynamic
    var spacing: Spacing
    var transitions: Transitions
    var typography: Typography
    var zIndex: ZIndex
}

@JsModule("@mui/material/styles/createTheme")
@JsNonModule
private external val createMuiThemeModule: dynamic


@Suppress("UnsafeCastFromDynamic")
fun createMuiTheme(themeOptions: ThemeOptions? = null, args: dynamic = null): Theme {
    val ourThemeOptions = themeOptions ?: jsObject {  }
    return createMuiThemeModule.default(ourThemeOptions, args)
}