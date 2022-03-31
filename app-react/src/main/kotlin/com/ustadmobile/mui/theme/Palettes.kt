package com.ustadmobile.mui.theme

external interface TypeText {
    var primary: String
    var secondary: String
    var disabled: String
    var hint: String
}

external interface TypeAction {
    var active: String
    var hover: String
    var selected: String
    var disabled: String
    var disabledBackground: String
}

external interface TypeBackground {
    var default: String
    var paper: String
}

external interface SimplePaletteColorOptions {
    var light: String? get() = definedExternally; set(value) = definedExternally
    var main: String
    var dark: String? get() = definedExternally; set(value) = definedExternally
    var contrastText: String? get() = definedExternally; set(value) = definedExternally
}

external interface PaletteColor {
    var light: String
    var main: String
    var dark: String
    var contrastText: String
}

external interface TypeObject {
    var text: TypeText
    var action: TypeAction
    var background: TypeBackground
}

external interface CommonColors {
    var black: String
    var white: String
}

external interface Color {
    var _50: String
    var _100: String
    var _200: String
    var _300: String
    var _400: String
    var _500: String
    var _600: String
    var _700: String
    var _800: String
    var _900: String
    var A100: String
    var A200: String
    var A400: String
    var A700: String
}

external interface Palette {
    var common: CommonColors
    var mode: String //export type PaletteType = 'light' | 'dark';
    var contrastThreshold: Int
    var tonalOffset: Float
    var primary: PaletteColor
    var secondary: PaletteColor
    var error: PaletteColor
    var success: PaletteColor
    var info: PaletteColor
    var grey: Color
    var text: TypeText
    var divider: String
    var action: TypeAction
    var background: TypeBackground
    var getContrastText: (color: String) -> String
}

external interface PaletteOptions {
    var common: Any? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var mode: String? get() = definedExternally; set(value) = definedExternally
    var primary: dynamic /* Any? | SimplePaletteColorOptions */ get() = definedExternally; set(value) = definedExternally
    var secondary: dynamic /* Any? | SimplePaletteColorOptions */ get() = definedExternally; set(value) = definedExternally
    var error: dynamic /* Any? | SimplePaletteColorOptions */ get() = definedExternally; set(value) = definedExternally
    var grey: Any? get() = definedExternally; set(value) = definedExternally
    var text: Any? get() = definedExternally; set(value) = definedExternally
    var divider: String? get() = definedExternally; set(value) = definedExternally
    var action: Any? get() = definedExternally; set(value) = definedExternally
    var background: Any? get() = definedExternally; set(value) = definedExternally
    var getContrastText: ((color: String) -> String)? get() = definedExternally; set(value) = definedExternally
}
