package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.navigation.RouteManager.defaultDestination
import com.ustadmobile.navigation.RouteManager.lookupDestinationName
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.redux.ReduxAppStateManager.subscribe
import com.ustadmobile.redux.ReduxStore
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.getViewNameFromUrl
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.RBuilder
import react.setState


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
       /* themeContext.Consumer { theme ->

            styledDiv {
                css (mainComponentWrapperContainer)

                //Loading indicator
                mLinearProgress {
                    css(mainComponentProgressIndicator)
                    attrs.asDynamic().id = "um-progress"
                    attrs.color = if(isDarkModeActive()) MLinearProgressColor.secondary
                    else MLinearProgressColor.primary
                }

                styledDiv {
                    css(mainComponentContainer)

                    mAppBar(position = MAppBarPosition.fixed) {
                        css (if(currentDestination.showNavigation) mainComponentAppBar
                        else mainComponentAppBarWithNoNav)

                        mToolbar {
                            attrs.asDynamic().id = "um-toolbar"
                            attrs.asDynamic().onClick = {
                                GlobalScope.launch {
                                    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)
                                    repo.exportDatabase()
                                }
                            }
                            css(mainComponentToolbarMargins)

                            umGridContainer {

                                mHidden(xsDown = currentDestination.showSearch) {
                                    umItem(if(currentDestination.showSearch) MGridSize.cells1 else MGridSize.cells9, MGridSize.cells5){
                                        css{marginTop = 4.px}
                                        mToolbarTitle(appState.appToolbar.title ?: "")
                                    }
                                }

                                umItem(if(currentDestination.showSearch) MGridSize.cells9 else MGridSize.cells1,MGridSize.cells6){

                                    styledDiv {

                                        css{
                                            +mainComponentSearch
                                            display = displayProperty(currentDestination.showSearch)
                                        }

                                        styledDiv {
                                            css(mainComponentSearchIcon)
                                            mIcon("search")
                                        }

                                        val inputProps = object: RProps {
                                            val className = "${StyleManager.name}-mainComponentInputSearchClass"
                                            val id = "um-search"
                                        }

                                        mInput(placeholder = "${getString(MessageID.search)}...",
                                            disableUnderline = true) {
                                            attrs.inputProps = inputProps
                                        }
                                    }
                                }

                                umItem(MGridSize.cells2, MGridSize.cells1){
                                    mAvatar {
                                        css {
                                            display = displayProperty(currentDestination.showNavigation)
                                            +mainComponentProfileOuterAvatar
                                        }

                                        attrs {
                                            onClick = {
                                                systemImpl.go(AccountListView.VIEW_NAME, mapOf(), this)
                                            }
                                        }

                                        mAvatar{
                                            css (mainComponentProfileInnerAvatar)
                                            mTypography("${activeAccount?.firstName?.first()}",
                                                align = MTypographyAlign.center,
                                                variant = MTypographyVariant.h5){
                                                css{
                                                    marginTop = (1.5).px
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
                            attrs.id = "main-content"
                            renderRoutes()
                        }
                    }

                    if(currentDestination.showNavigation){
                        renderBottomNavigation()
                    }

                    mFab("","",
                        color = MColor.secondary) {
                        attrs.id = "um-fab"
                        css{
                            display = Display.none
                            +mainComponentFab
                        }
                    }
                }
                renderSnackBar()
            }
        }

        errorBoundary(errorFallBack(getString(MessageID.error))){
            mCssBaseline()

        }*/
    }

    private fun RBuilder.renderBottomNavigation(){
      /*  mHidden(smUp = true) {
            mBottomNavigation(currentDestination, true,
                onChange = { _, value -> setState {
                val destination = value as UstadDestination
                systemImpl.go(destination.view, mapOf(),this)
            }}) {
                css (mainComponentBottomNav)

                destinationList.filter { it.icon != null && it.view != SettingsView.VIEW_NAME }.forEach { destination ->
                    destination.icon?.let {
                        mBottomNavigationAction(getString(destination.labelId),
                            mIcon(it, addAsChild = false),
                            value = destination,
                            showLabel = true)
                    }
                }
            }
        }*/
    }

    private fun RBuilder.renderSideNavigation(){
       /* val p: MPaperProps = jsObject { }
        p.asDynamic().style = kotlinext.js.js {
            position = "relative"; width = drawerWidth.px.value; display = "block"; height =
            "100%"; minHeight = "100vh"
        }*/

       /* mHidden(xsDown = true) {
            mDrawer(true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = p) {
                styledDiv {
                    css(mainComponentBrandIconContainer)
                    styledImg(src = "assets/brand-logo.png"){
                        css(mainComponentBrandIcon)
                    }
                }
                mDivider {
                    css(defaultFullWidth)
                }
                themeContext.Consumer { _ ->
                    mList {
                        css (mainComponentSideNavMenuList)
                        destinationList.filter { it.icon != null }.forEach { destination ->
                            destination.icon?.let {
                                mListItemWithIcon(it, getString(destination.labelId),
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
        }*/
    }

    private fun RBuilder.renderSnackBar(){
       /* mSnackbar(altBuilder.span { +"${appState.appSnackBar.message}"},
            open = appState.appSnackBar.message != null,
            horizAnchor = MSnackbarHorizAnchor.center, autoHideDuration = 3000,
            onClose = { _, _ ->
                dispatch(ReduxSnackBarState())
            }) {

            if(!appState.appSnackBar.actionLabel.isNullOrBlank()){
                attrs.action = altBuilder.div {
                    mButton("${appState.appSnackBar.actionLabel}", color = MColor.secondary,
                        variant = MButtonVariant.text, size = MButtonSize.small,
                        onClick = {
                            appState.appSnackBar.onClick
                            dispatch(ReduxSnackBarState())
                        })
                }
            }
        }*/
    }


    override fun componentWillUnmount() {
        appStateChangeListener = {}
    }
}

fun RBuilder.mainComponent() = child(MainComponent::class) {}