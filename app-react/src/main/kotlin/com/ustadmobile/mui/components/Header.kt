package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.mui.common.Area
import csstype.*
import mui.system.sx
import mui.material.*
import mui.material.styles.TypographyVariant.h6
import react.*
import react.dom.html.ReactHTML.div
import web.html.HTMLElement

val DEFAULT_APPBAR_HEIGHT = 64

external interface HeaderProps: Props {
    var appUiState: AppUiState

    var setAppBarHeight: (Int) -> Unit

}

val Header = FC<HeaderProps> { props ->
    var theme by useContext(ThemeContext)
    val appBarRef = useRef<HTMLElement>(null)

    useEffect(appBarRef.current?.clientHeight){
        appBarRef.current?.also {
            props.setAppBarHeight(it.clientHeight)
        }
    }

    AppBar {
        position = AppBarPosition.fixed
        ref = appBarRef
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

            if(props.appUiState.searchState.visible) {
                AppBarSearch {
                    onTextChanged = props.appUiState.searchState.onSearchTextChanged
                    searchText = props.appUiState.searchState.searchText
                }
            }

        }
    }
}