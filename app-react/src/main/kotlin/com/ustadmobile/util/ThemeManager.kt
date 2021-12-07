package com.ustadmobile.util

import com.ustadmobile.mui.theme.Theme
import com.ustadmobile.mui.theme.ThemeOptions
import com.ustadmobile.mui.theme.createMuiTheme
import kotlinx.browser.window

/**
 * Responsible for styling the app by customizing theme colors.
 *
 * To have custom colors just change primary and secondary main colors
 */
object ThemeManager {

    private val themeOptions: ThemeOptions = js("({palette: { type: 'placeholder'," +
            " primary: {main: 'placeholder'}, secondary: {main: 'placeholder'}}})")

    /**
     * Check if the device theme setting is current on dark mode.
     * @return TRUE if is in dark mode otherwise FALSE.
     */
    val isDarkModeActive:()-> Boolean = {
        window.matchMedia("(prefers-color-scheme: dark)").matches
    }

    /**
     * Create a theme to be applied to the app
     */
    fun createAppTheme(): Theme {
        themeOptions.palette?.mode = if(isDarkModeActive()) "dark" else "light"
        themeOptions.palette?.primary.light = "#48a999" //if(isDarkModeActive()) "#424242" else "#48a999"
        themeOptions.palette?.primary.main = "#00796b" //if(isDarkModeActive()) "#303030" else "#00796b"
        themeOptions.palette?.primary.dark = "#005E55" // if(isDarkModeActive()) "#00796b" else "#005E55"
        themeOptions.palette?.primary.contrastText = "#fff"
        themeOptions.palette?.secondary.main = if(isDarkModeActive()) "#00796b" else "#ff9800"
        themeOptions.palette?.secondary.contrastText = if(isDarkModeActive()) "#fff" else "#000000"
        return createMuiTheme(themeOptions)
    }
}