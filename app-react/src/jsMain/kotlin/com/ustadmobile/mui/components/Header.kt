package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppBarColors
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.view.components.UstadPersonAvatar
import js.objects.jso
import web.cssom.*
import mui.system.sx
import mui.material.*
import mui.material.styles.TypographyVariant.Companion.h6
import org.kodein.di.direct
import org.kodein.di.instance
import react.*
import react.dom.aria.AriaHasPopup
import react.dom.aria.ariaExpanded
import react.dom.aria.ariaHasPopup
import react.dom.aria.ariaLabel
import react.dom.aria.ariaLabelledBy
import react.dom.html.ReactHTML.div
import react.router.useLocation
import react.router.useNavigate
import web.dom.Element
import web.dom.document
import web.html.HTMLElement
import mui.icons.material.MoreVert as MoreVertIcon
import mui.icons.material.Settings as SettingsIcon
import mui.icons.material.Menu as MenuIcon

private val ROOT_LOCATIONS = UstadViewModel.ROOT_DESTINATIONS.map {
    "/$it"
}

external interface HeaderProps: Props {
    var appUiState: AppUiState

    var setAppBarHeight: (Int) -> Unit

    var showMenuIcon: Boolean

    var onClickMenuIcon: () -> Unit

    var sidebarVisible: Boolean
}

val Header = FC<HeaderProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val appBarRef = useRef<HTMLElement>(null)
    val strings = useStringProvider()
    var overflowAnchor by useState<Element?> { null }
    val navigateFn = useNavigate()
    val location = useLocation()
    val leadingActionButtonVal = props.appUiState.leadingActionButton
    val isSelectionColors = props.appUiState.appBarColors == AppBarColors.SELECTION_MODE
    val contentColor = if(!isSelectionColors) {
        theme.palette.primary.contrastText
    }else {
        theme.palette.secondary.contrastText
    }

    val appDi = useRequiredContext(DIContext)
    val accountManager: UstadAccountManager = appDi.di.direct.instance()
    val currentSession: UserSessionWithPersonAndEndpoint by accountManager.currentUserSessionFlow
        .collectAsState(
            UserSessionWithPersonAndEndpoint(
                userSession = UserSession(),
                person = Person(),
                endpoint = Endpoint("")
            )
        )

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
        color = if(props.appUiState.appBarColors == AppBarColors.STANDARD) {
            AppBarColor.primary
        }else {
            AppBarColor.secondary
        }

        sx {
            gridArea = Area.Header
            zIndex = integer(1_500)

            //If the sidebar is visible, then we need to use margin to make space for the logo in
            //the top left.
            marginLeft = if(props.sidebarVisible) {
                Sizes.Sidebar.Width
            }else {
                0.px
            }
            width = if(props.sidebarVisible) {
                "calc(100% - ${Sizes.Sidebar.Width})".unsafeCast<Width>()
            }else {
                100.pct
            }
        }

        if(props.appUiState.loadingState.loadingState == LoadingUiState.State.INDETERMINATE) {
            LinearProgress {
                sx {
                    position = Position.absolute
                    width = 100.pct
                    height = 4.px
                    marginTop = (Sizes.Header.HeightInPx - 2).px
                }
                color = LinearProgressColor.secondary
                id = "header_progress_bar"

                variant =  LinearProgressVariant.indeterminate
            }
        }


        Toolbar {
            /* If there is a leading action button (e.g. close selection), then show that.
             * If not, and we are in responsive mode, show the shelf menu to open drawer.
             */
            when {
                leadingActionButtonVal != null -> {
                    UstadActionButtonIcon {
                        actionButton = leadingActionButtonVal
                        color = contentColor
                    }
                }

                props.showMenuIcon && props.appUiState.navigationVisible -> {
                    IconButton {
                        ariaLabel = strings[MR.strings.menu]
                        onClick = {
                            props.onClickMenuIcon()
                        }

                        MenuIcon {
                            sx {
                                color = contentColor
                            }
                        }
                    }
                }
            }

            Typography{
                sx { flexGrow = number(1.0) }
                id = "appbar_title"
                variant = h6
                noWrap = true
                component = div

                + appBarTitle
            }

            if(location.pathname in ROOT_LOCATIONS && !props.appUiState.hideSettingsIcon) {
                Tooltip {
                    title = ReactNode(strings[MR.strings.settings])

                    IconButton {
                        id = "settings_button"
                        onClick = {
                            navigateFn("/${SettingsViewModel.DEST_NAME}")
                        }
                        ariaLabel = strings[MR.strings.settings]

                        SettingsIcon {
                            sx {
                                color = contentColor
                            }
                        }
                    }
                }
            }

            props.appUiState.actionButtons.forEach {
                UstadActionButtonIcon {
                    actionButton = it
                    color = contentColor
                }
            }

            if(props.appUiState.searchState.visible) {
                AppBarSearch {
                    onTextChanged = props.appUiState.searchState.onSearchTextChanged
                    searchText = props.appUiState.searchState.searchText
                }
            }

            /**
             * On the right hand side:
             * Show action bar button (e.g. save) if present - highest priority
             * Else show overflow menu if present
             * Else show avatar icon
             */
            if(props.appUiState.actionBarButtonState.visible) {
                Button {
                    sx {
                        color = theme.palette.common.white
                    }
                    id = "actionBarButton"
                    disabled = !props.appUiState.actionBarButtonState.enabled

                    onClick = {
                        props.appUiState.actionBarButtonState.onClick()
                    }
                    + props.appUiState.actionBarButtonState.text
                }
            }else if(props.appUiState.overflowItems.isNotEmpty()) {
                val overflowAnchorVal = overflowAnchor

                IconButton {
                    ariaHasPopup = AriaHasPopup.`true`
                    ariaExpanded = overflowAnchorVal != null
                    onClick = {
                        overflowAnchor = if(overflowAnchor == null) {
                            it.currentTarget
                        }else {
                            null
                        }
                    }

                    id = "header_overflow_menu_expand_button"

                    MoreVertIcon {
                        sx {
                            color = theme.palette.primary.contrastText
                        }
                    }
                }

                if(overflowAnchorVal != null) {
                    Menu {
                        id = "header_overflow_menu"
                        open = true
                        anchorEl = {
                            overflowAnchorVal
                        }
                        sx {
                            marginTop = theme.spacing(2)
                        }

                        onClose = {
                            overflowAnchor = null
                        }

                        MenuListProps = jso {
                            ariaLabelledBy = "header_overflow_menu_expand_button"
                        }

                        props.appUiState.overflowItems.forEach { item ->
                            MenuItem {
                                onClick = {
                                    overflowAnchor = null
                                    item.onClick()
                                }
                                + item.label
                            }
                        }
                    }
                }

            }else if(props.appUiState.userAccountIconVisible) {
                IconButton {
                    id = "header_avatar"
                    onClick = {
                        navigateFn.invoke(AccountListViewModel.DEST_NAME)
                    }

                    UstadPersonAvatar {
                        personName = currentSession.person.fullName()
                        pictureUri = currentSession.personPicture?.personPictureThumbnailUri
                    }
                }
            }
        }
    }
}
