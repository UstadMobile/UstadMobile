package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import mui.material.Link
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import web.cssom.TextAlign
import web.window.WindowTarget

val UstadPoweredByLink = FC<Props> {
    val strings = useStringProvider()
    Link {
        sx {
            textAlign = TextAlign.center
        }

        href = "https://www.ustadmobile.com/"
        variant = TypographyVariant.caption
        target = WindowTarget._blank

        + strings[MR.strings.powered_by]
    }
}