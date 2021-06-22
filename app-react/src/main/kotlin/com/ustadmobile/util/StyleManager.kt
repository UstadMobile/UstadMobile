package com.ustadmobile.util

import com.ccfraser.muirwik.components.spacingUnits
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.down
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.redux.ReduxAppStateManager
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
object StyleManager: StyleSheet("ComponentStyles", isStatic = true), DIAware {

    private val theme = ReduxAppStateManager.getCurrentState().appTheme?.theme!!

    private val systemImpl : UstadMobileSystemImpl by instance()

    private val fullWidth = 100.pct

    const val drawerWidth = 240

    val tabletAndHighEnd = Breakpoint.md

    val alignTextToStart by css {
        textAlign = TextAlign.start
    }

    val defaultFullWidth by css {
        width = LinearDimension("100%")
    }

    val splashComponentContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
    }

    val splashComponentPreloadContainer by css{
        left = LinearDimension("50%")
        top = LinearDimension("50%")
        marginLeft = (-100).px
        marginTop = (-50).px
        position =  Position.fixed
        height = 200.px
        width = 200.px
    }

    val splashComponentLoadingImage by css{
        width = 180.px
        marginLeft = 10.px
        position = Position.absolute
    }

    val splashComponentPreloadProgressBar by css {
        width = 200.px
        marginTop = 140.px
        position = Position.absolute
    }

    val mainComponentErrorPaper by css{
        padding(2.spacingUnits)
        marginBottom = 2.spacingUnits
        color = Color.red
    }

    val mainComponentProgressIndicator by css {
        width = 100.pc
        display = Display.none
    }

    val mainComponentContainer by css {
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        width = 100.pct
    }

    val mainComponentWrapperContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
        backgroundColor = Color(theme.palette.background.paper)
    }

    val mainComponentAppBarRtlSideNav by css {
        position = Position.absolute
        marginLeft = drawerWidth.px
        width = fullWidth
    }

    val mainComponentAppBar by css{
        position = Position.absolute
        marginLeft = drawerWidth.px
        media(theme.breakpoints.up(Breakpoint.md)) {
            width = fullWidth - drawerWidth.px
        }

        media(theme.breakpoints.down(Breakpoint.sm)) {
            width = fullWidth
        }
    }

    val mainComponentContentContainer by css {
        height = LinearDimension("100vh")
        flexGrow = 1.0
        minWidth = 0.px
        backgroundColor = Color(theme.palette.background.default)
        media(theme.breakpoints.down(Breakpoint.xs)){
            height = LinearDimension("91vh")
        }
    }

    val mainComponentBottomNav by css {
        position = Position.fixed
        bottom = 0.px
        left = 0.px
        right = 0.px
    }

    val mainComponentBrandIconContainer by css {
        height = 43.px
        marginTop = 20.px
        width = LinearDimension("100%")
        padding = "0px ${if(systemImpl.isRtlActive()) 20 else 0 }px 0 ${if(systemImpl.isRtlActive()) 0 else 20 }px"
    }

    val mainComponentBrandIcon by css{
        width = LinearDimension("80%")
        height = LinearDimension("60%")
    }

    val mainComponentSideNavMenuList by css {
        backgroundColor = Color(theme.palette.background.paper)
        width = drawerWidth.px
    }

    val mainComponentProfileOuterAvatar by css {
        width = 40.px
        height = 40.px
        cursor = Cursor.pointer
        margin = "0px ${if(systemImpl.isRtlActive()) 40 else 0 }% 0 ${if(systemImpl.isRtlActive()) 0 else 40 }%"
        backgroundColor = Color(theme.palette.primary.light)
    }

    val mainComponentProfileInnerAvatar by css {
        width = 36.px
        height = 36.px
        margin = "2px ${if(systemImpl.isRtlActive()) 2.4 else 0 }px 0 ${if(systemImpl.isRtlActive()) 0 else 2 }px"
        color = Color.white
        backgroundColor = Color(theme.palette.primary.dark)
    }


    val mainComponentSearch by css {
        position = Position.relative
        marginTop = 4.px
        display = Display.none
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
        paddingRight = (if(systemImpl.isRtlActive()) 60 else 0).px
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

    val mainComponentToolbarMargins by css{
        if(systemImpl.isRtlActive()){
            paddingRight = 30.px
        }else{
            paddingLeft = 30.px
        }
    }

    val mainComponentFab by css{
        if(!systemImpl.isRtlActive()) right = 15.px
        if(systemImpl.isRtlActive()) left = 15.px
        position = Position.fixed
        bottom = 70.px
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            bottom = 15.px
        }
    }

    val languageComponentLanguageSelectorFormControl by css {
        margin(1.spacingUnits)
        minWidth = 120.px
        width = LinearDimension.auto
        display = Display.flex
        position = Position.fixed
        height = LinearDimension.auto
        bottom = 16.px
    }

    fun displayProperty(visible:Boolean, isFlexLayout:Boolean = false): Display {
       return if(visible)
           if(isFlexLayout) Display.block else Display.flex
       else Display.none
    }

    override val di: DI
        get() = ReduxAppStateManager.getCurrentState().appDi.di

}