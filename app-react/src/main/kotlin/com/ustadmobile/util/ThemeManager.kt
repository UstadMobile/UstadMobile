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
        themeOptions.palette?.primary.light = "#b2fef7"
        themeOptions.palette?.primary.main = "#80cbc4"
        themeOptions.palette?.primary.dark = "#4f9a94"
        themeOptions.palette?.primary.contrastText = "#fff"
        themeOptions.palette?.secondary.main = "#ffd600"
        themeOptions.palette?.secondary.contrastText = "#fff"
        return createMuiTheme(themeOptions)
    }
}