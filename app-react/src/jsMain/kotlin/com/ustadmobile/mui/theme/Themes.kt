package com.ustadmobile.mui.theme

//As per MUI showcase

import js.core.jso
import mui.material.PaletteMode.Companion.dark
import mui.material.PaletteMode.Companion.light
import mui.material.styles.createTheme
import web.cssom.Color
import kotlin.js.json

object Themes {
    val Light = createTheme(
        /**
         * This should roughly follow the theme code as generated by:
         *
         * https://zenoo.github.io/mui-theme-creator/
         */
        jso {
            palette = jso {
                mode = light
                primary = json(
                    "main" to Color("#D9291C")
                )
                secondary = json(
                    "main" to Color("#45B383")
                )
            }
        }
    )


    @Suppress("unused") //Reserved for future use
    val Dark = createTheme(
        jso {
            palette = jso {
                mode = dark
            }
        }
    )
}

