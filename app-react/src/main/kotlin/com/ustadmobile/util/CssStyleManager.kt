package com.ustadmobile.util

import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.Theme
import com.ccfraser.muirwik.components.styles.up
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.ms
import styled.StyleSheet

/**
 * Responsible for styling HTML elements, to customize particular
 * element just check the defined style constants.
 * They are named as per component
 */
object UmStyles: StyleSheet("ComponentStyles", isStatic = true) {

    val appContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
    }

    val fab by css{
        position = Position.fixed
        right = 15.px
        bottom = 15.px
        zIndex = 99999
    }

    val progressIndicator by css {
        width = LinearDimension("100%")
    }

    val preloadComponentCenteredDiv by css{
        height = 200.px
        width = 200.px
        left = LinearDimension("50%")
        top = LinearDimension("50%")
        marginLeft = (-100).px
        marginTop = (-50).px
        position =  Position.fixed
    }

    val preloadComponentProgressBar by css {
        width = 200.px
        marginTop = 140.px
        position = Position.absolute
    }

    val preloadComponentCenteredImage by css{
        width = 180.px
        marginLeft = 10.px
        position = Position.absolute
    }


    val mainComponentRootDiv by css {
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        width = 100.pct
    }

    val mainComponentContentArea by css {
        height = LinearDimension.fillAvailable
        flexGrow = 1.0
        minWidth = 0.px
    }

    val mainComponentSearchIcon by css {
        width = 9.spacingUnits
        height = 100.pct
        position = Position.absolute
        pointerEvents = PointerEvents.none
        display = Display.flex
        alignItems = Align.center
        justifyContent = JustifyContent.center
    }

    val mainComponentSearch by css {
        val theme = window.asDynamic().theme as Theme
        position = Position.relative
        borderRadius = theme.shape.borderRadius.px
        backgroundColor = Color(fade(theme.palette.common.white, 0.15))
        hover {
            backgroundColor = Color(fade(theme.palette.common.white, 0.25))
        }
        marginLeft = 0.px
        marginRight = 30.px
        width = 100.pct
        media(theme.breakpoints.up(Breakpoint.sm)) {
            marginLeft = 1.spacingUnits
            width = LinearDimension.auto
        }
    }

    val mainComponentInputSearch by css {
        val theme = window.asDynamic().theme as Theme
        paddingTop = 1.spacingUnits
        paddingRight = 1.spacingUnits
        paddingBottom = 1.spacingUnits
        paddingLeft = 10.spacingUnits
        transition += Transition("width", theme.transitions.duration.standard.ms, Timing.easeInOut, 0.ms)
        width = 100.pct
        media(theme.breakpoints.up(Breakpoint.sm)) {
            width = 120.px
            focus {
                width = 300.px
            }
        }
    }

    val listContainer by css {
        //display = Display.inlineFlex
        padding(1.spacingUnits)
    }

    val horizontalList by css {
        width = LinearDimension.auto
        backgroundColor = Color((window.asDynamic().theme as Theme).palette.background.paper)
    }

    val listCreateNewContainer by css {
        padding = "10px"
    }

    val listItemCreateNewDiv by css {
        display = Display.inlineFlex
    }

    val listCreateNewIcon by css {
        fontSize = LinearDimension("2em")
        marginTop = 19.px
    }

    val listCreateNewLabel by css{
        fontSize = LinearDimension("1.2em")
        fontWeight = FontWeight("400")
    }
}