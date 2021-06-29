package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mFab
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItemWithIcon
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.navigation.RouteManager.defaultDestination
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.navigation.RouteManager.findDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.subscribe
import com.ustadmobile.redux.ReduxSnackBarState
import com.ustadmobile.redux.ReduxStore
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.drawerWidth
import com.ustadmobile.util.StyleManager.mainComponentBrandIcon
import com.ustadmobile.util.StyleManager.mainComponentBrandIconContainer
import com.ustadmobile.util.StyleManager.mainComponentAppBar
import com.ustadmobile.util.StyleManager.mainComponentBottomNav
import com.ustadmobile.util.StyleManager.mainComponentProfileOuterAvatar
import com.ustadmobile.util.StyleManager.mainComponentProfileInnerAvatar
import com.ustadmobile.util.StyleManager.mainComponentContainer
import com.ustadmobile.util.StyleManager.mainComponentContentContainer
import com.ustadmobile.util.StyleManager.mainComponentFab
import com.ustadmobile.util.StyleManager.mainComponentProgressIndicator
import com.ustadmobile.util.StyleManager.mainComponentSearch
import com.ustadmobile.util.StyleManager.mainComponentSearchIcon
import com.ustadmobile.util.StyleManager.mainComponentSideNavMenuList
import com.ustadmobile.util.StyleManager.mainComponentWrapperContainer
import com.ustadmobile.util.StyleManager.mainComponentToolbarMargins
import com.ustadmobile.util.StyleManager.tabletAndHighEnd
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ext.*
import kotlinext.js.jsObject
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.id
import react.RBuilder
import react.RProps
import react.RState
import react.dom.div
import react.dom.span
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg


class MainComponent(props: RProps): UstadBaseComponent<RProps, RState>(props){

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
        val destination = findDestination(getViewNameFromUrl()) ?: defaultDestination

        destination.takeIf { it.labelId != 0 && it.labelId != MessageID.content}?.apply {
            title = getString(labelId)
        }

        setState {
            currentDestination = destination
        }
    }


    override fun RBuilder.render() {
        errorBoundary(errorFallBack(getString(MessageID.error))){
            mCssBaseline()
            themeContext.Consumer { theme ->

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
                            css (mainComponentAppBar)

                            mToolbar {
                                attrs.asDynamic().id = "um-toolbar"
                                css(mainComponentToolbarMargins)

                                umGridContainer {

                                    umItem(MGridSize.cells10, MGridSize.cells5){
                                        css{marginTop = 4.px}
                                        mToolbarTitle(appState.appToolbar.title ?: "")
                                    }

                                    mHidden(xsDown = true) {
                                        umItem(MGridSize.cells6){

                                            styledDiv {

                                                css{
                                                    +mainComponentSearch
                                                    media(theme.breakpoints.up(tabletAndHighEnd)){
                                                        display = displayProperty(currentDestination.showSearch)
                                                    }
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
                                    }

                                    umItem(MGridSize.cells2, MGridSize.cells1){
                                        mAvatar {
                                            css {
                                                display = displayProperty(currentDestination.showNavigation)
                                                +mainComponentProfileOuterAvatar
                                            }

                                            attrs {
                                                onClick = {}
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

                        mFab(appState.appFab.icon ?: "",
                            appState.appFab.title?.uppercase() ?: "",
                            color = MColor.secondary,
                            onClick = appState.appFab.onClick) {
                            css{
                                display = displayProperty(appState.appFab.visible, true)
                                +mainComponentFab
                            }
                        }
                    }
                    renderSnackBar()
                }
            }
        }
    }

    private fun RBuilder.renderBottomNavigation(){
        mHidden(smUp = true) {
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
        }
    }

    private fun RBuilder.renderSideNavigation(){
        val p: MPaperProps = jsObject { }
        p.asDynamic().style = kotlinext.js.js {
            position = "relative"; width = drawerWidth.px.value; display = "block"; height =
            "100%"; minHeight = "100vh"
        }

        mHidden(xsDown = true) {
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
        }
    }

    private fun RBuilder.renderSnackBar(){
        mSnackbar(altBuilder.span { +"${appState.appSnackBar.message}"},
            open = appState.appSnackBar.message != null,
            horizAnchor = MSnackbarHorizAnchor.center, autoHideDuration = 5000,
            onClose = { _, _ ->
                dispatch(ReduxSnackBarState())
            }) {
            attrs.action = altBuilder.div {
                mButton("${appState.appSnackBar.actionLabel}", color = MColor.secondary,
                    variant = MButtonVariant.text, size = MButtonSize.small,
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

fun RBuilder.mainComponent() = child(MainComponent::class) {}