package com.ustadmobile.util

import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.css.*
import kotlinx.css.properties.Transforms
import kotlinx.css.properties.transform
import styled.StyleSheet

object UmStyles: StyleSheet("ComponentStyles", isStatic = true) {

    val rootDiv by css {
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        width = 100.pct
    }

    val listRoot by css {
        //display = Display.inlineFlex
        padding(1.spacingUnits)
    }


    val buttonMargin by css {
        position = Position.fixed
        bottom = 16.px
        right = 16.px

    }

    val textField by css {
        marginLeft = 1.spacingUnits
        marginRight = 1.spacingUnits
    }

    val preloadingDiv by css {
        position =  Position.fixed
        left = 50.pc
        top = 50.pc
    }
}