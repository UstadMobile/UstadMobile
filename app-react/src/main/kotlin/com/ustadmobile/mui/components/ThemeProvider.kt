package com.ustadmobile.mui.components

import com.ustadmobile.mui.theme.Theme
import com.ustadmobile.mui.theme.ThemeOptions
import com.ustadmobile.mui.theme.createMuiTheme
import kotlinx.css.LinearDimension
import mui.system.ThemeProvider
import react.Props
import react.RBuilder
import react.RHandler
import react.createContext

private val themeOptions: ThemeOptions = js("({typography: {useNextVariants: true}})")

val defaultTheme: Theme = createMuiTheme(themeOptions)

val themeContext = createContext(defaultTheme)

val Int.spacingUnits get() = LinearDimension((defaultTheme.spacing.asDynamic()(this)).toString())

@JsModule("@mui/material/styles/useTheme")
@JsNonModule
private external val useThemeDefault: dynamic

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun useTheme(): Theme = useThemeDefault.default() as Theme

fun RBuilder.umThemeProvider(
    theme: Theme = defaultTheme,
    handler: RHandler<Props>? = null
) = ThemeProvider {
    attrs.theme = theme
    if (handler != null) handler()
}