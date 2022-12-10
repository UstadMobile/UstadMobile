package com.ustadmobile.view.components

import csstype.number
import mui.icons.material.CheckBoxOutlineBlank
import mui.system.sx
import react.FC
import react.Props

/**
 * Simple placeholder blank icon
 */
val UstadBlankIcon = FC<Props> {
    CheckBoxOutlineBlank {
        sx {
            opacity = number(0.0)
        }
    }
}