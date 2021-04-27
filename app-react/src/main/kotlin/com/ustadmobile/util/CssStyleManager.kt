package com.ustadmobile.util

import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.up
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
object CssStyleManager: StyleSheet("ComponentStyles", isStatic = true) {

    private val theme = StateManager.getCurrentState().theme!!

    val isMobile: Boolean = js("/iPhone|iPad|iPod|Android/i.test(navigator.userAgent)")

    val appContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
    }

    val bottomFixedElements by css{
        display = Display.flex
        position = Position.fixed
        zIndex = 99999
        height = LinearDimension.auto
        width = LinearDimension("100%")
        flexDirection = FlexDirection.column
        bottom = 0.px
        backgroundColor = Color.transparent
    }

    val fab by css{
        right = 15.px
        bottom = 15.px
    }

    val defaultMarginTop  by css{
        marginTop = (if(isMobile) 7 else 2).spacingUnits
    }

    val progressIndicator by css {
        width = 100.pc
    }

    val mainComponentAvatarOuter by css {
        width = 40.px
        height = 40.px
        cursor = Cursor.pointer
        backgroundColor = Color(theme.palette.primary.light)
    }

    val mainComponentAvatarInner by css {
        width = 36.px
        height = 36.px
        margin = "2px 0 0 2.4px"
        color = Color.white
        backgroundColor = Color(theme.palette.primary.dark)
    }

    val splashComponentPreloadDiv by css{
        left = LinearDimension("50%")
        top = LinearDimension("50%")
        marginLeft = (-100).px
        marginTop = (-50).px
        position =  Position.fixed
        height = 200.px
        width = 200.px
    }

    val splashComponentProgressBar by css {
        width = 200.px
        marginTop = 140.px
        position = Position.absolute
    }

    val splashComponentCenteredImage by css{
        width = 180.px
        marginLeft = 10.px
        position = Position.absolute
    }


    val mainComponentContainer by css {
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        width = 100.pct
    }

    val mainComponentTabsScroller by css{
        display = Display.block
    }

    val mainComponentContentContainer by css {
        height = LinearDimension("${if(isMobile) 91 else 100}vh")
        flexGrow = 1.0
        minWidth = 0.px
        paddingLeft = 1.spacingUnits
        paddingRight = 1.spacingUnits
        backgroundColor = Color(theme.palette.background.default)
    }

    val mainComponentContents by css{
        height = LinearDimension("100vh")
        overflowY = Overflow.auto
        paddingLeft = 2.spacingUnits
        paddingRight = 2.spacingUnits
        paddingTop = 2.spacingUnits
        paddingBottom = (if(isMobile) 16 else 10).spacingUnits
        backgroundColor = Color(theme.palette.background.default)
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

    val mainComponentErrorPaper by css{
        padding(2.spacingUnits)
        marginBottom = 2.spacingUnits
        color = Color.red
    }

    val mainComponentSearch by css {
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

    val ustadListViewComponentContainer by css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        height = LinearDimension("100%")
    }

    val entryListItemContainer by css {
        width = LinearDimension.auto
        display = Display.flex
        flexDirection = FlexDirection.row
    }

    val entryListItemImage by css{
        marginRight = 20.px
        width = (8.5).pc
        height = 150.px
    }

    val contentEntryListExtraOptions by css{
        display = Display.flex
        flexDirection = FlexDirection.row
        marginBottom = 6.px
    }

    val entryListItemInfo by css {
        width = LinearDimension.auto
        display = Display.flex
        flexDirection = FlexDirection.column
    }


    val horizontalList by css {
        width = LinearDimension.auto
        backgroundColor = Color(theme.palette.background.paper)
    }

    val listCreateNewContainer by css {
        padding = "10px"
    }

    val contentEntryListAvatar by css {
        height = 3.spacingUnits
        width = 3.spacingUnits
    }

    val contentEntryListIcon by css {
        fontSize = LinearDimension("0.65em")
        marginBottom = 4.px
    }

    val listItemCreateNewDiv by css {
        display = Display.inlineFlex
        marginLeft = 16.px
    }

    val listCreateNewIcon by css {
        fontSize = LinearDimension("2.5em")
        marginTop = 5.px
    }


    val entryDetailComponentContainer by css {
        width = LinearDimension.auto
        height = LinearDimension("100%")
        flexDirection = if(isMobile) FlexDirection.column else FlexDirection.row
        padding(2.spacingUnits)
    }

    val entryDetailComponentEntryImage by css {
        width = LinearDimension("100%")
        height = LinearDimension.initial
    }

    val entryDetailComponentEntryImageAndButtonContainer by css {
        width = LinearDimension("${if(isMobile) 100 else 40}%")
        display = Display.flex
        marginRight = LinearDimension("${if(isMobile) 0 else 3}%")
        flexDirection = FlexDirection.column
        paddingBottom = (if(isMobile) 0 else 5).spacingUnits
    }

    val entryDetailComponentEntryExtraInfo by css {
        width = LinearDimension("${if(isMobile) 100 else 57}%")
        flexDirection = FlexDirection.column
        marginTop = (if(isMobile) 3 else 0).spacingUnits
        paddingBottom = 10.spacingUnits
    }

    val chipSet by css{
        display = Display.flex
        justifyContent = JustifyContent.start
        flexWrap = FlexWrap.wrap
    }

    val loginComponentContainer by css{
        position = Position.relative
        left = LinearDimension("50%")
        float = Float.left
    }

    val loginComponentForm by css{
        position = Position.relative
        left = LinearDimension("-50%")
        float = Float.left
        display = Display.flex
        flexDirection = FlexDirection.column
        width = 390.px
        marginTop = LinearDimension("30%")
    }

    val loginComponentFormElementsMargin by css {
        marginLeft = 1.spacingUnits
        marginRight = 1.spacingUnits
        width = LinearDimension("100%")
    }

    val errorTextMessage by css {
        color = Color.red
    }

    val tabsContainer by css {
        flexGrow = 1.0
        backgroundColor = Color(theme.palette.background.paper)
    }

    val responsiveIframe by css{
        overflow = Overflow.hidden
        width = LinearDimension("100%")
        minHeight = LinearDimension("100%")
        backgroundColor = Color.transparent
        border = "0px"
    }

    val responsiveMedia by css{
        overflow = Overflow.hidden
        width = LinearDimension("100%")
        minHeight = LinearDimension("100%")
        height = LinearDimension("100%")
        backgroundColor = Color.transparent
    }

}