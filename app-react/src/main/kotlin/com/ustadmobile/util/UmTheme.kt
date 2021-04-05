package com.ustadmobile.util

import com.ccfraser.muirwik.components.Colors
import com.ccfraser.muirwik.components.styles.Theme
import com.ccfraser.muirwik.components.styles.ThemeOptions
import com.ccfraser.muirwik.components.styles.createMuiTheme
import com.ustadmobile.util.UmUtil.isDarkModeEnabled
import kotlinx.css.Color

/**
 * Responsible for styling the app by customizing theme colors,
 * To have custom colors just change primary and secondary main colors
 */
object UmTheme {

    private val themeOptions: ThemeOptions = js("({palette: { type: 'placeholder'," +
            " primary: {main: 'placeholder'}, secondary: {main: 'placeholder'}}})")

    const val primaryColor = "#00796b"

    const val secondaryColor = "#ff9800"

    /**
     * Create app theme
     */
    fun getUmTheme(): Theme {
        themeOptions.palette?.type = if (isDarkModeEnabled()) "dark" else "light"
        themeOptions.palette?.primary.main = if(isDarkModeEnabled()) primaryColor else primaryColor
        themeOptions.palette?.secondary.main = secondaryColor
        return createMuiTheme(themeOptions)
    }
}