package com.ustadmobile.util

import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.ms
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import styled.StyleSheet

/**
 * Responsible for styling HTML elements, to customize particular
 * element just check the defined style constants.
 * They are named as per component
 */
object CssStyleManager: StyleSheet("ComponentStyles", isStatic = true), DIAware {

    val theme = StateManager.getCurrentState().theme!!

    val systemImpl : UstadMobileSystemImpl by instance()

    val isRTLSupported = systemImpl.isRTLSupported(this)

    val defaultMarginEnd = "0 ${if(isRTLSupported) 20 else 0}px 0 ${if(isRTLSupported) 0 else 20}px"

    private val defaultContainerWidth = LinearDimension("74.8vw")

    val tabletAndHighEnd = Breakpoint.md

    val isMobile: Boolean = js("/iPhone|iPad|iPod|Android/i.test(navigator.userAgent)")

    const val drawerWidth = 240

    val zeroPx = 0.px

    var fullWidth = 100.pct

    val appContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
    }

    val mainToolbar by css{
        if(isRTLSupported){
            paddingRight = 30.px
        }else{
            paddingLeft = 30.px
        }
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

    val mainBrandIconContainer by css {
        height = 43.px
        marginTop = 20.px
        width = LinearDimension("100%")
        padding = "0px ${if(isRTLSupported) 20 else 0 }px 0 ${if(isRTLSupported) 0 else 20 }px"
    }

    val mainBrandIcon by css{
        width = LinearDimension("80%")
        height = LinearDimension("60%")
    }

    val mainComponentFab by css{
        if(!isRTLSupported) right = 15.px
        if(isRTLSupported) left = 15.px
        position = Position.fixed
        bottom = 70.px
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            bottom = 15.px
        }
    }

    val defaultMarginTop  by css{
        marginTop = 2.spacingUnits
    }

    val progressIndicator by css {
        width = 100.pc
    }

    val mainComponentAvatarOuter by css {
        width = 40.px
        height = 40.px
        cursor = Cursor.pointer
        margin = "0px ${if(isRTLSupported) 40 else 0 }% 0 ${if(isRTLSupported) 0 else 40 }%"
        backgroundColor = Color(theme.palette.primary.light)
    }

    val mainComponentAvatarInner by css {
        width = 36.px
        height = 36.px
        margin = "2px ${if(isRTLSupported) 2.4 else 0 }px 0 ${if(isRTLSupported) 0 else 2 }px"
        color = Color.white
        backgroundColor = Color(theme.palette.primary.dark)
    }


    val mainLanguageSelectorFormControl by css {
        margin(1.spacingUnits)
        minWidth = 120.px
        width = LinearDimension.auto
        display = Display.flex
        position = Position.fixed
        height = LinearDimension.auto
        bottom = 16.px
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
        paddingBottom = 17.spacingUnits
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            paddingBottom = 15.spacingUnits
        }
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
        marginTop = 4.px
        borderRadius = theme.shape.borderRadius.px
        backgroundColor = Color(fade(theme.palette.common.white, 0.15))
        hover {
            backgroundColor = Color(fade(theme.palette.common.white, 0.25))
        }
        marginLeft = 0.px
        marginRight = 30.px
        media(theme.breakpoints.up(Breakpoint.sm)) {
            marginLeft = 1.spacingUnits
            width = LinearDimension.auto
        }
    }

    val mainComponentInputSearchClass by css {
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
        color = Color.inherit
        paddingRight = (if(isRTLSupported) 60 else 0).px
    }

    val listComponentContainer by css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        height = LinearDimension("100%")
        width = LinearDimension("100%")
    }

    val entryListItemContainer by css {
        width = defaultContainerWidth
        display = Display.flex
        flexDirection = FlexDirection.row
    }

    val entryListItemImageContainer by css{
        marginRight = (if(isRTLSupported) 0 else 20).px
        marginLeft = (if(isRTLSupported) 20 else 0).px
        width = (8.5).pc
        height = 150.px
    }

    val alignTextToStart by css {
        textAlign = TextAlign.start
    }

    val contentEntryListExtraOptions by css{
        display = Display.flex
        flexDirection = FlexDirection.row
        marginBottom = 6.px
    }

    val entryListItemInfo by css {
        width = LinearDimension.inherit
        display = Display.flex
        flexDirection = FlexDirection.column
    }


    val horizontalList by css {
        width = LinearDimension("100%")
        backgroundColor = Color(theme.palette.background.paper)
    }

    val listCreateNewContainer by css {
        padding = "10px"
    }

    val contentEntryListContentAvatarClass by css {
        height = 3.spacingUnits
        width = 3.spacingUnits
    }

    val contentEntryListContentTyeIconClass by css {
        fontSize = LinearDimension("0.65em")
        marginBottom = 4.px
    }

    val fallBackAvatarClass by css {
        fontSize = LinearDimension("1em")
        marginBottom = 4.px
    }

    val listItemCreateNewDiv by css {
        display = Display.inlineFlex
        marginLeft = 16.px
    }

    val listCreateNewIconClass by css {
        fontSize = LinearDimension("2.5em")
        marginTop = 5.px
    }


    val entryDetailComponentEntryImageAndButtonContainer by css {
        width = LinearDimension("${if(isMobile) 100 else 40}%")
        display = Display.flex
        margin = "0 ${if(isMobile || isRTLSupported) 0 else 3}% 0 ${if(isMobile || isRTLSupported) 3 else 0}%"
        flexDirection = FlexDirection.column
        paddingBottom = (if(isMobile) 0 else 5).spacingUnits
    }

    val entryDetailOpenBtn by css {
        margin = "2% 1.5% 0% 1.5%"
        width = LinearDimension("98%")
    }

    val entryDetailEntryExtraInfo by css {
        width = LinearDimension("100%")
        flexDirection = FlexDirection.column
        margin = "0 2% 0 2%"
        paddingBottom = 10.spacingUnits
    }

    val chipSet by css{
        display = Display.flex
        justifyContent = JustifyContent.start
        flexWrap = FlexWrap.wrap
    }

    val tabsContainer by css {
        flexGrow = 1.0
        backgroundColor = Color(theme.palette.background.paper)
    }

    val selectionContainer by css{
        paddingTop = 12.px
        paddingBottom = 12.px
        backgroundColor = Color(theme.palette.background.default)
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


    val personListItemAvatar by css {
        width = 50.px
        height = 50.px
        margin = "2px ${if(isRTLSupported) 2.4 else 0 }px 0 ${if(isRTLSupported) 0 else 2.4 }px"
        color = Color(theme.palette.background.paper)
        backgroundColor = Color(theme.palette.action.disabled)
    }

    val personDetailComponentActions by css{
        display = Display.flex
        flexDirection = FlexDirection.column
        alignContent = Align.center
        alignItems = Align.center
        paddingBottom = 16.px
        padding = "16px 30px 16px 30px"
        cursor = Cursor.pointer
        width = LinearDimension("100%")
        hover {
            backgroundColor = Color(theme.palette.action.selected)
        }
    }

    val personDetailComponentActionIcon by css{
        marginBottom = 10.px
    }

    val formMidWidthTextField by css{
        marginLeft = 1.spacingUnits
        marginRight = 1.spacingUnits
        marginTop = 2.spacingUnits
        width = LinearDimension("70%")
    }

    val formMinWidthTextField by css{
        marginLeft = 1.spacingUnits
        marginRight = 1.spacingUnits
        marginTop = 2.spacingUnits
        width = LinearDimension("97%")
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            width = LinearDimension("45%")
            margin = "16px ${if(isRTLSupported) 3 else 0}% 0 ${if(isRTLSupported) 0 else 3}%"
        }
    }

    val alignContentCenterContainer by css{
        textAlign = TextAlign.center
    }

    val defaultFullWidth by css {
        width = LinearDimension("100%")
    }

    val errorTextClass by css{
        color = Color(theme.palette.error.main)
        marginLeft = LinearDimension("${if(isRTLSupported) 0 else 16}px")
        marginRight= LinearDimension("${if(isRTLSupported) 16 else 0}px")
    }

    val imageContainer by css {
        width = LinearDimension("100%")
        margin = "1.5%"
        textAlign = TextAlign.center
    }

    val entityImageClass by css {
        textAlign = TextAlign.center
        position = Position.relative
        width = LinearDimension("98%")
        height = LinearDimension("300px")
    }

    val entityThumbnailClass by css {
        textAlign = TextAlign.center
        position = Position.relative
        width = LinearDimension("98%")
        height = LinearDimension("120px")
    }

    val entityImageIconClass by css {
        fontSize = LinearDimension("3em")
    }

    val detailIconClass by css {
        fontSize = LinearDimension("2em")
        marginTop = 3.px
    }


    override val di: DI
        get() = StateManager.getCurrentState().di

}