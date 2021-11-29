package com.ustadmobile.view

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.AccountListView
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
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.mainComponentAppBar
import com.ustadmobile.util.StyleManager.mainComponentAppBarWithNoNav
import com.ustadmobile.util.StyleManager.mainComponentBottomNav
import com.ustadmobile.util.StyleManager.mainComponentBrandIcon
import com.ustadmobile.util.StyleManager.mainComponentBrandIconContainer
import com.ustadmobile.util.StyleManager.mainComponentContainer
import com.ustadmobile.util.StyleManager.mainComponentContentContainer
import com.ustadmobile.util.StyleManager.mainComponentFab
import com.ustadmobile.util.StyleManager.mainComponentProfileInnerAvatar
import com.ustadmobile.util.StyleManager.mainComponentProfileOuterAvatar
import com.ustadmobile.util.StyleManager.mainComponentProgressIndicator
import com.ustadmobile.util.StyleManager.mainComponentSearch
import com.ustadmobile.util.StyleManager.mainComponentSearchIcon
import com.ustadmobile.util.StyleManager.mainComponentSideNavMenuList
import com.ustadmobile.util.StyleManager.mainComponentToolbarMargins
import com.ustadmobile.util.StyleManager.mainComponentWrapperContainer
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ext.*
import kotlinext.js.jsObject
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.display
import kotlinx.css.marginTop
import mui.material.InputBaseComponentProps
import mui.material.InputBaseProps
import mui.material.PaperProps
import org.kodein.di.instance
import org.kodein.di.on
import react.Props
import react.RBuilder
import react.ReactElement
import react.dom.div
import react.dom.span
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg


class MainComponent(props: UmProps): UstadBaseComponent<UmProps, UmState>(props){

    private var activeAccount: UmAccount? = null

    private val altBuilder = RBuilder()

    override var viewName: String? = null

    private var appState: ReduxAppState = ReduxAppState()

    private lateinit var currentDestination: UstadDestination

    private var appStateChangeListener : (ReduxStore) -> Unit = { store ->
        setState {
            appState = store.appState
        }
    }

    private val activeAccountObserver:(UmAccount?) -> Unit = {
        GlobalScope.launch(Dispatchers.Main) {
            setState {
                activeAccount = it
            }
        }
    }

    override fun componentWillMount() {
        super.componentWillMount()
        subscribe(appStateChangeListener)

        accountManager.activeAccountLive.observeWithLifecycleOwner(this,
            activeAccountObserver)

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
        val destination = lookupDestinationName(getViewNameFromUrl()) ?: defaultDestination

        destination.takeIf { it.labelId != 0 && it.labelId != MessageID.content}?.apply {
            title = getString(labelId)
        }

        setState {
            currentDestination = destination
        }
    }


    override fun RBuilder.render() {
        themeContext.Consumer { theme ->

            styledDiv {
                css (mainComponentWrapperContainer)

                //Loading indicator
                umLinearProgress(color = if(isDarkModeActive()) LinearProgressColor.secondary
                else LinearProgressColor.primary) {
                    css(mainComponentProgressIndicator)
                    attrs.asDynamic().id = "um-progress"
                }

                styledDiv {
                    css(mainComponentContainer)

                    umAppBar(position = AppBarPosition.fixed) {
                        css (if(currentDestination.showNavigation) mainComponentAppBar
                        else mainComponentAppBarWithNoNav)

                        umToolbar {
                            attrs.asDynamic().id = "um-toolbar"
                            attrs.asDynamic().onClick = {
                                GlobalScope.launch {
                                    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)
                                    repo.exportDatabase()
                                }
                            }
                            css(mainComponentToolbarMargins)

                            umGridContainer {

                                umHidden(xsDown = currentDestination.showSearch) {
                                    umItem(if(currentDestination.showSearch) GridSize.column1 else GridSize.column9, GridSize.column5){
                                        css{marginTop = LinearDimension("4px") }
                                        umToolbarTitle(appState.appToolbar.title ?: "")
                                    }
                                }

                                umItem(if(currentDestination.showSearch) GridSize.column9 else GridSize.column1,GridSize.column6){

                                    styledDiv {

                                        css{
                                            +mainComponentSearch
                                            display = displayProperty(currentDestination.showSearch)
                                        }

                                        styledDiv {
                                            css(mainComponentSearchIcon)
                                            umIcon("search")
                                        }

                                        val inputProps = object: Props {
                                            val className = "${StyleManager.name}-mainComponentInputSearchClass"
                                            val id = "um-search"
                                        }

                                        umInput(placeholder = "${getString(MessageID.search)}...",
                                            disableUnderline = true) {
                                            attrs.inputProps = inputProps as InputBaseComponentProps
                                        }
                                    }
                                }

                                umItem(GridSize.column2, GridSize.column1){
                                    umAvatar {
                                        css {
                                            display = displayProperty(currentDestination.showNavigation)
                                            +mainComponentProfileOuterAvatar
                                        }

                                        attrs {
                                            onClick = {
                                                systemImpl.go(AccountListView.VIEW_NAME, mapOf(), this)
                                            }
                                        }

                                        umAvatar{
                                            css (mainComponentProfileInnerAvatar)
                                            umTypography("${activeAccount?.firstName?.first()}",
                                                align = TypographyAlign.center,
                                                variant = TypographyVariant.h5){
                                                css{
                                                    marginTop = LinearDimension("1.5px")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
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
                            renderRoutes()
                        }
                    }

                    if(currentDestination.showNavigation){
                        renderBottomNavigation()
                    }

                    umFab("","",
                        color = UMColor.secondary) {
                        attrs.asDynamic().id = "um-fab"
                        css{
                            display = Display.none
                            +mainComponentFab
                        }
                    }
                }
                renderSnackBar()
            }
        }

 /*       errorBoundary(errorFallBack(getString(MessageID.error))){
            umCssBaseline()

        }*/
    }

    private fun RBuilder.renderBottomNavigation(){
        umHidden(smUp = true) {
            umBottomNavigation(currentDestination, true) {
                css (mainComponentBottomNav)
                attrs.onChange = { _, value -> setState {
                    val destination = value as UstadDestination
                    systemImpl.go(destination.view, mapOf(),this)
                }}
                destinationList.filter { it.icon != null && it.view != SettingsView.VIEW_NAME }.forEach { destination ->
                    destination.icon?.let {
                        umBottomNavigationAction(label = getString(destination.labelId).asDynamic() as ReactElement?,
                            it.asDynamic() as ReactElement?,
                            value = destination,
                            showLabel = true)
                    }
                }
            }
        }
    }

    private fun RBuilder.renderSideNavigation(){
        val p: PaperProps = jsObject { }
        p.asDynamic().style = kotlinext.js.js {
            position = "relative"; width = drawerWidth.px.value; display = "block"; height =
            "100%"; minHeight = "100vh"
        }

        umHidden(xsDown = true) {
            umDrawer(true, DrawerAnchor.left, DrawerVariant.permanent, paperProps = p) {
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
                                    onClick = {
                                        systemImpl.go(destination.view, mapOf(),this)
                                    },
                                    selected = currentDestination == destination){
                                    css(alignTextToStart)
                                }
                            }
                        }
                    }
                    renderLanguages(systemImpl)
                }
            }
        }
    }

    private fun RBuilder.renderSnackBar(){
        umSnackbar(altBuilder.span { +"${appState.appSnackBar.message}"}.toString(),
            open = appState.appSnackBar.message != null,
            horizAnchor = SnackbarHorizAnchor.center, autoHideDuration = 3000) {

            if(!appState.appSnackBar.actionLabel.isNullOrBlank()){
                attrs.action = altBuilder.div {
                    umButton("${appState.appSnackBar.actionLabel}", color = UMColor.secondary,
                        variant = ButtonVariant.text, size = ButtonSize.small,
                        onClick = {
                            appState.appSnackBar.onClick
                            dispatch(ReduxSnackBarState())
                        })
                } as ReactElement
            }
        }
    }


    override fun componentWillUnmount() {
        appStateChangeListener = {}
    }
}

fun RBuilder.mainComponent() = child(MainComponent::class) {}