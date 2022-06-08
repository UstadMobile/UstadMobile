package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.navigation.RouteManager.defaultDestination
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.navigation.RouteManager.lookupDestinationName
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.subscribe
import com.ustadmobile.redux.ReduxSnackBarState
import com.ustadmobile.redux.ReduxStore
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.hideOnMobile
import com.ustadmobile.util.StyleManager.mainComponentBottomNav
import com.ustadmobile.util.StyleManager.mainComponentBrandIcon
import com.ustadmobile.util.StyleManager.mainComponentBrandIconContainer
import com.ustadmobile.util.StyleManager.mainComponentContainer
import com.ustadmobile.util.StyleManager.mainComponentContentContainer
import com.ustadmobile.util.StyleManager.mainComponentFab
import com.ustadmobile.util.StyleManager.mainComponentProgressIndicator
import com.ustadmobile.util.StyleManager.mainComponentSideNavMenuList
import com.ustadmobile.util.StyleManager.mainComponentWrapperContainer
import com.ustadmobile.util.StyleManager.showOnMobile
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ext.appBarSpacer
import com.ustadmobile.view.ext.renderRoutes
import com.ustadmobile.view.ext.umTopBar
import kotlinext.js.jsObject
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.css.padding
import mui.material.PaperProps
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg


class MainComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props){

    private var activeAccount: UmAccount? = null

    private var appState: ReduxAppState = ReduxAppState()

    private lateinit var currentDestination: UstadDestination

    private var appStateChangeListener : (ReduxStore) -> Unit = { store ->
        setState {
            appState = store.appState
        }
    }

    override fun componentWillMount() {
        super.componentWillMount()
        subscribe(appStateChangeListener)

        window.addEventListener("hashchange", {
            onDestinationChanged()
        })

        onDestinationChanged()
    }


    /**
     * Similar to Android MainActivity NavController destination listener,
     * this trigger change on state to update visibility of frame items
     * i.e Side Nav, Bottom nav e.tc
     */
    private fun onDestinationChanged() {
        var viewName: String? = null
        try {
            viewName = UstadUrlComponents.parse(window.location.href).viewName
        }catch(e: Exception) {
            //not an UstadUrl (yet)
        }

        val destination = lookupDestinationName(viewName) ?: defaultDestination
        destination.takeIf { it.labelId != 0 && it.labelId != MessageID.content}?.apply {
            ustadComponentTitle = getString(labelId)
        }

        setState {
            currentDestination = destination
            activeAccount = accountManager.activeAccount
        }

        window.setTimeout({
            val settings = document.getElementById("home-${MessageID.settings}")
            settings?.asDynamic()?.style?.display = if(activeAccount?.admin == false) "none" else "flex"
        }, 500)
    }


    override fun RBuilder.render() {
        themeContext.Consumer { _ ->

            styledDiv {
                css (mainComponentWrapperContainer)

                //Loading indicator
                umLinearProgress(color = if(isDarkModeActive()) UMColor.secondary
                else UMColor.primary) {
                    css(mainComponentProgressIndicator)
                    attrs.asDynamic().id = "um-progress"
                }

                styledDiv {
                    css(mainComponentContainer)

                    umTopBar(appState,
                        currentDestination,
                        if(systemImpl.isRtlActive()) "..." else "" +
                                "${getString(MessageID.search)} " +
                                if(systemImpl.isRtlActive()) "" else "...",
                        activeAccount?.firstName){
                        systemImpl.go(AccountListView.VIEW_NAME, mapOf(), this)
                    }

                    if(currentDestination.showNavigation){
                        renderSideNavigation()
                    }

                    // Main content area, this div holds the contents
                    styledDiv {
                        css(mainComponentContentContainer)
                        appBarSpacer()
                        styledDiv {
                            attrs.asDynamic().id = "main-content"
                            renderRoutes(di)
                        }
                    }

                    if(currentDestination.showNavigation){
                        renderBottomNavigation()
                    }

                    umFab("","",
                        id = "um-fab",
                        color = UMColor.secondary) {
                        css{
                            display = Display.none
                            +mainComponentFab
                        }
                    }
                }
                renderSnackBar()
            }
        }
    }

    private fun RBuilder.renderBottomNavigation(){
        umBottomNavigation(currentDestination, true) {
            css {
                +mainComponentBottomNav
                +showOnMobile
            }
            attrs.onChange = { _, value -> setState {
                val destination = value as UstadDestination
                systemImpl.go(destination.view, mapOf(),this)
            }}
            val extraMenuToShow = mutableListOf(SettingsView.VIEW_NAME)
            if(!accountManager.activeAccount.admin){
                extraMenuToShow+=ReportListView.VIEW_NAME
            }

            destinationList.filter { it.icon != null && extraMenuToShow.indexOf(it.view) == -1 }.forEach { destination ->
                destination.icon?.let {
                    umBottomNavigationAction(
                        label = getString(destination.labelId), it,
                        value = destination,
                        showLabel = true
                    )
                }
            }
        }
    }

    private fun RBuilder.renderSideNavigation(){
        val p: PaperProps = jsObject { }
        p.asDynamic().style = kotlinext.js.js {
            position = "relative"; display = "block"; height =
            "100%"; minHeight = "100vh"
        }

        umDrawer(true, DrawerAnchor.left, DrawerVariant.permanent, paperProps = p) {
            css(hideOnMobile)
            styledDiv {
                css(mainComponentBrandIconContainer)
                styledImg(src = "assets/brand-logo.png"){
                    css(mainComponentBrandIcon)
                }
            }
            umDivider {
                css(defaultFullWidth)
            }
            themeContext.Consumer { _ ->
                umList {
                    css (mainComponentSideNavMenuList)
                    destinationList.filter { it.icon != null }.forEach { destination ->
                        destination.icon?.let {
                            umListItemWithIcon(it, getString(destination.labelId),
                                divider = destination.divider ,
                                id = "home-${destination.labelId}",
                                onClick = {
                                    systemImpl.go(destination.view, mapOf(),this)
                                },
                                selected = currentDestination == destination){
                                css{
                                    +alignTextToStart
                                    padding = "8px 16px"
                                    display = if(destination.labelId == MessageID.settings) Display.none else Display.flex
                                }
                            }
                        }
                    }
                }
                renderLanguages(systemImpl)
            }
        }
    }

    private fun RBuilder.renderSnackBar(){
        umSnackbar("${appState.appSnackBar.message}",
            open = appState.appSnackBar.message != null,
            autoHideDuration = 3000, onClose = {
                dispatch(ReduxSnackBarState())
            }) {

            if(!appState.appSnackBar.actionLabel.isNullOrBlank()){
                attrs.action = umButton("${appState.appSnackBar.actionLabel}",
                    variant = ButtonVariant.text,
                    size = ButtonSize.medium,
                    onClick = {
                        appState.appSnackBar.onClick
                        dispatch(ReduxSnackBarState())
                    })
            }
        }
    }


    override fun componentWillUnmount() {
        appStateChangeListener = {}
    }
}

fun RBuilder.renderMainComponent() = child(MainComponent::class) {}