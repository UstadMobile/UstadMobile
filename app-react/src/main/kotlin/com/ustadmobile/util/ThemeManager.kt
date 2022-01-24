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
        themeOptions.palette?.primary.light = "#67daff"
        themeOptions.palette?.primary.main = "#02a9f4"
        themeOptions.palette?.primary.dark = "#007ac1"
        themeOptions.palette?.primary.contrastText = "#fff"
        themeOptions.palette?.secondary.main = "#7b1fa2"
        themeOptions.palette?.secondary.contrastText = "#fff"
        return createMuiTheme(themeOptions)
    }
}