package com.ustadmobile.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.redux.ReduxAppStateManager
import kotlinx.css.*
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

    val theme = ReduxAppStateManager.getCurrentState().appTheme?.theme!!

    val systemImpl : UstadMobileSystemImpl by instance()

    val splashMainComponentContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
    }

    val splashPreloadContainer by css{
        left = LinearDimension("50%")
        top = LinearDimension("50%")
        marginLeft = (-100).px
        marginTop = (-50).px
        position =  Position.fixed
        height = 200.px
        width = 200.px
    }

    val splashLoadingImage by css{
        width = 180.px
        marginLeft = 10.px
        position = Position.absolute
    }

    val splashPreloadProgressBar by css {
        width = 200.px
        marginTop = 140.px
        position = Position.absolute
    }

    override val di: DI
        get() = ReduxAppStateManager.getCurrentState().appDi.di

}