package com.ustadmobile.port.android.ui.theme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.toughra.ustadmobile.R

private val DarkColorPalette = darkColors(
    primary = Color(R.color.primaryDarkColor),
    primaryVariant = Color(R.color.primaryDarkColor),
    secondary = Color(R.color.primaryDarkColor)
)

private val LightColorPalette = lightColors(
    primary = Color(R.color.primaryDarkColor),
    primaryVariant = Color(R.color.primaryDarkColor),
    secondary = Color(R.color.primaryDarkColor)

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun UstadMobileTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}