package com.ustadmobile.core.util.ext

import web.cssom.ColorProperty
import web.cssom.rgb

fun Int.rgbColorProperty(): ColorProperty {
    return rgb(
        red = (this shr 24) and 0xff,
        green = (this shr 16) and 0xff,
        blue = (this shr 8) and 0xff
    )
}
