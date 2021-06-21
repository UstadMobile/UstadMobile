package com.ustadmobile.util

import com.ccfraser.muirwik.components.styles.Theme
import com.ccfraser.muirwik.components.styles.ThemeOptions
import com.ccfraser.muirwik.components.styles.createMuiTheme
import kotlinx.browser.window

/**
 * Responsible for styling the app by customizing theme colors.
 *
 * To have custom colors just change primary and secondary main colors
 */
object ThemeManager {

    private val themeOptions: ThemeOptions = js("({palette: { type: 'placeholder'," +
            " primary: {main: 'placeholder'}, secondary: {main: 'placeholder'}}})")

    private const val primaryColor = "#00796b"

    private const val primaryLightColor = "#48a999"

    private const val primaryDarkColor = "#005E55"

    private const val secondaryColor = "#ff9800"

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
        themeOptions.palette?.type = if (isDarkModeActive()) "dark" else "light"
        themeOptions.palette?.primary.light = if(isDarkModeActive()) primaryLightColor else primaryLightColor
        themeOptions.palette?.primary.main = if(isDarkModeActive()) primaryColor else primaryColor
        themeOptions.palette?.primary.dark = if(isDarkModeActive()) primaryDarkColor else primaryDarkColor
        themeOptions.palette?.secondary.main = secondaryColor
        return createMuiTheme(themeOptions)
    }
}