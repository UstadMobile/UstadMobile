package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.mui.common.Area
import web.cssom.*
import mui.system.sx
import mui.material.*
import mui.material.styles.TypographyVariant.Companion.h6
import react.*
import react.dom.html.ReactHTML.div
import web.dom.document
import web.html.HTMLElement

external interface HeaderProps: Props {
    var appUiState: AppUiState

    var setAppBarHeight: (Int) -> Unit

}

val Header = FC<HeaderProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val appBarRef = useRef<HTMLElement>(null)
    val strings = useStringProvider()

    var appBarTitle by useState {
        strings[MR.strings.app_name]
    }

    val appUiStateTitle = props.appUiState.title
    useEffect(appUiStateTitle) {
        if(appUiStateTitle != null && appUiStateTitle != appBarTitle) {
            appBarTitle = appUiStateTitle
            document.title = "${strings[MR.strings.app_name]} : $appUiStateTitle"
        }
    }

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

                + appBarTitle
            }

            if(props.appUiState.searchState.visible) {
                AppBarSearch {
                    onTextChanged = props.appUiState.searchState.onSearchTextChanged
                    searchText = props.appUiState.searchState.searchText
                }
            }

            if(props.appUiState.actionBarButtonState.visible) {
                Button {
                    sx {
                        color = theme.palette.common.white
                    }
                    id = "actionBarButton"
                    onClick = {
                        props.appUiState.actionBarButtonState.onClick()
                    }
                    + props.appUiState.actionBarButtonState.text
                }
            }

            if(!props.appUiState.actionBarButtonState.visible && props.appUiState.userAccountIconVisible) {
                HeaderAvatar()
            }
        }
    }
}