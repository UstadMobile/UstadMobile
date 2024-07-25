package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import web.cssom.TextAlign
import web.cssom.pct
import mui.icons.material.CropFree as CropFreeIcon
import com.ustadmobile.core.MR
import react.dom.aria.ariaLabel
import web.cssom.Auto
import web.cssom.Display

val UstadNothingHereYet = FC<Props> {
    val strings = useStringProvider()

    Stack {
        sx {
            width = 100.pct
            textAlign = TextAlign.center
        }

        direction = responsive(StackDirection.column)

        CropFreeIcon {
            sx {
                marginLeft = Auto.auto
                marginRight = Auto.auto
                display = Display.block
            }
            ariaLabel = ""
        }

        Typography {
            + strings[MR.strings.nothing_here_yet]
        }
    }

}
