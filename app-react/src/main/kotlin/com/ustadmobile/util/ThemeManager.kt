package com.ustadmobile.util

import com.ccfraser.muirwik.components.styles.Theme
import com.ccfraser.muirwik.components.styles.ThemeOptions
import com.ccfraser.muirwik.components.styles.createMuiTheme
import com.ustadmobile.util.UmReactUtil.isDarkModeEnabled

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
     * Create a theme to be used applied to the app
     */
    fun createAppTheme(): Theme {
        themeOptions.palette?.type = if (isDarkModeEnabled()) "dark" else "light"
        themeOptions.palette?.primary.light = if(isDarkModeEnabled()) primaryLightColor else primaryLightColor
        themeOptions.palette?.primary.main = if(isDarkModeEnabled()) primaryColor else primaryColor
        themeOptions.palette?.primary.dark = if(isDarkModeEnabled()) primaryDarkColor else primaryDarkColor
        themeOptions.palette?.secondary.main = secondaryColor
        return createMuiTheme(themeOptions)
    }
}