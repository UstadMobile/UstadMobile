package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.mui.common.Area
import csstype.integer
import mui.material.AppBar
import mui.material.AppBarPosition
import mui.material.Toolbar
import mui.material.Typography
import mui.system.sx
import react.FC
import react.Props
import react.useContext
import csstype.number
import mui.material.styles.TypographyVariant.h6
import react.dom.html.ReactHTML.div

external interface HeaderProps: Props {
    var appUiState: AppUiState
}

val Header = FC<HeaderProps> { props ->
    var theme by useContext(ThemeContext)

    AppBar {
        position = AppBarPosition.fixed
        sx {
            gridArea = Area.Header
            zIndex = integer(1_500)
        }

        Toolbar {
            Typography{
                sx { flexGrow = number(1.0) }
                variant = h6
                noWrap = true
                component = div

                + (props.appUiState.title ?: "Ustad Mobile")
            }
        }
    }
}