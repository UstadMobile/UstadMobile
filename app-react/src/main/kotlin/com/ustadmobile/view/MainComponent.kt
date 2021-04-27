package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mFab
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItemWithIcon
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.model.statemanager.GlobalState
import com.ustadmobile.model.statemanager.GlobalStateSlice
import com.ustadmobile.model.statemanager.SnackBarState
import com.ustadmobile.model.statemanager.ToolbarTabs
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.appContainer
import com.ustadmobile.util.CssStyleManager.bottomFixedElements
import com.ustadmobile.util.CssStyleManager.fab
import com.ustadmobile.util.CssStyleManager.isMobile
import com.ustadmobile.util.CssStyleManager.mainComponentAvatarInner
import com.ustadmobile.util.CssStyleManager.mainComponentAvatarOuter
import com.ustadmobile.util.CssStyleManager.mainComponentContainer
import com.ustadmobile.util.CssStyleManager.mainComponentContentContainer
import com.ustadmobile.util.CssStyleManager.mainComponentContents
import com.ustadmobile.util.CssStyleManager.mainComponentErrorPaper
import com.ustadmobile.util.CssStyleManager.mainComponentSearch
import com.ustadmobile.util.CssStyleManager.mainComponentSearchIcon
import com.ustadmobile.util.CssStyleManager.progressIndicator
import com.ustadmobile.util.RouteManager.destinationList
import com.ustadmobile.util.RouteManager.findDestination
import com.ustadmobile.util.RouteManager.renderRoutes
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmReactUtil.drawerWidth
import com.ustadmobile.util.UmReactUtil.fullWidth
import com.ustadmobile.util.UmReactUtil.isDarkModeEnabled
import com.ustadmobile.util.UmReactUtil.zeroPx
import kotlinext.js.jsObject
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.dom.addClass
import react.*
import react.dom.div
import react.dom.span
import styled.css
import styled.styledDiv

interface MainProps: RProps {
    var currentDestination: UmReactDestination
}


class MainComponent(props: MainProps): UstadBaseComponent<MainProps, RState>(props){

    private var activeAccount: UmAccount? = null

    private var isRTLSupport: Boolean = false

    private val altBuilder = RBuilder()

    private  var globalState: GlobalState = GlobalState()

    private lateinit var currentDestination: UmReactDestination

    private var stateChangeListener : (GlobalStateSlice) -> Unit = { slice ->
        document.getElementById("um-tabs")
            ?.querySelector("div.MuiTabs-scroller")
            ?.addClass("${CssStyleManager.name}-mainComponentTabsScroller")
        setState { globalState = slice.state }
    }

    private val mActiveUserObserver:(UmAccount?) -> Unit = {
        GlobalScope.launch(Dispatchers.Main) {
            setState { activeAccount = it }
        }
    }

    override fun RState.init(props: MainProps) {
        currentDestination = props.currentDestination
    }

    override fun componentDidMount() {
        super.componentDidMount()
        StateManager.subscribe(stateChangeListener)
        accountManager.activeAccountLive.observeWithLifecycleOwner(this,
            mActiveUserObserver)
    }

    override fun onComponentRefreshed(viewName: String?) {
        super.onComponentRefreshed(viewName)
        val destination = findDestination(viewName)
        if(destination != null){
            title = systemImpl.getString(destination.labelId, this)
            setState {
                currentDestination = destination
            }
        }
    }


    override fun RBuilder.render() {
        errorBoundary(errorFallbackComponent(systemImpl.getString(MessageID.error, this))){
            mCssBaseline()
            themeContext.Consumer { theme ->
                styledDiv {
                    css {
                        +appContainer
                        backgroundColor = Color(theme.palette.background.paper)
                    }

                    //Loading indicator
                    mLinearProgress {
                        css{
                            +progressIndicator
                            display = Display.none
                        }
                        attrs.asDynamic().id = "um-progress"
                        attrs {
                            color = if(isDarkModeEnabled()) MLinearProgressColor.secondary
                            else MLinearProgressColor.primary
                        }
                    }
                    styledDiv {
                        css { +mainComponentContainer }
                        mAppBar(position = MAppBarPosition.fixed) {
                            css {
                                val removeLeftMargin = isMobile or isRTLSupport or !currentDestination.showNavigation
                                position = Position.absolute
                                marginLeft = if(removeLeftMargin) zeroPx else drawerWidth
                                media(theme.breakpoints.up(Breakpoint.md)) {
                                    width = fullWidth - if(removeLeftMargin) zeroPx else drawerWidth
                                }
                                if(currentDestination.hasTabs){
                                    paddingBottom = 0.px
                                    marginBottom = 0.px
                                }
                            }

                            mToolbar {
                                attrs.asDynamic().id = "um-toolbar"
                                mHidden(xsDown = true){
                                    mToolbarTitle(globalState.title?:"")
                                }

                                styledDiv {
                                    css{
                                        +mainComponentSearch
                                        display = if(currentDestination.showSearch)
                                            Display.block else Display.none
                                    }
                                    styledDiv {
                                        css(mainComponentSearchIcon)
                                        mIcon("search")
                                    }
                                    val inputProps = object: RProps {
                                        val className = "${CssStyleManager.name}-mainComponentInputSearch"
                                        val id = "um-search"
                                    }
                                    mInput(placeholder = "${systemImpl.getString(MessageID.search,this)}...",
                                        disableUnderline = true) {
                                        attrs.inputProps = inputProps
                                        css {
                                            color = Color.inherit
                                        }
                                    }
                                }

                                mAvatar {
                                    css {
                                        display = if(currentDestination.showNavigation) Display.block else Display.none
                                        +mainComponentAvatarOuter
                                    }

                                    attrs {
                                        onClick = {}
                                    }

                                    mAvatar{
                                        css {
                                            +mainComponentAvatarInner
                                        }
                                        mTypography("${activeAccount?.firstName?.first()}",
                                            align = MTypographyAlign.center,
                                            variant = MTypographyVariant.h5){
                                            css{ marginTop = (1.5).px }
                                        }
                                    }
                                }
                            }

                            mTabs(globalState.selectedTab?:Any(), onChange = { _, value ->
                                StateManager.dispatch(ToolbarTabs(selected = value))
                                globalState.onTabChanged(value)}){
                                css{
                                    display = if(currentDestination.hasTabs) Display.block else Display.none
                                }
                                attrs.asDynamic().id = "um-tabs"
                                globalState.tabLabels.forEachIndexed{index,it ->
                                    mTab(it,globalState.tabKeys[index]){
                                        css{display = Display.block}
                                    }
                                }
                            }
                        }

                        if(currentDestination.showNavigation && !isMobile){
                            renderSideNavigation()
                        }

                        // Main content area, this div holds the contents
                        styledDiv {
                            css {
                                +mainComponentContentContainer
                                marginTop = LinearDimension(if(currentDestination.hasTabs) "56px" else "0px")
                            }
                            appBarSpacer()
                            styledDiv {
                                css (mainComponentContents)
                                renderRoutes()
                            }
                        }

                        styledDiv {
                            css{
                                +bottomFixedElements
                                display = if(globalState.showFab || isMobile) Display.flex else Display.none
                            }
                            if(globalState.showFab){
                                mFab(globalState.fabIcon, globalState.fabLabel.toUpperCase(), color = MColor.secondary) {
                                    css(fab)
                                    attrs {
                                        onClick = globalState.onFabClicked
                                    }
                                }
                            }

                            if(isMobile && currentDestination.showNavigation){
                                renderBottomNavigation()
                            }
                        }
                    }

                    mSnackbar(altBuilder.span { +"${globalState.snackBarMessage}"},
                        open = globalState.snackBarMessage != null,
                        horizAnchor = MSnackbarHorizAnchor.center, autoHideDuration = 5000,
                        onClose = { _, _ -> handleSnackBarClosing()}) {
                        attrs.action = altBuilder.div {
                            mButton("${globalState.snackBarActionLabel}", color = MColor.secondary,
                                variant = MButtonVariant.text, size = MButtonSize.small,
                                onClick = {
                                    globalState.onSnackActionClicked(it)
                                    handleSnackBarClosing()
                                })
                        }
                    }

                }
            }
        }
    }

    private fun handleSnackBarClosing(){
        StateManager.dispatch(SnackBarState())
    }

    private fun RBuilder.renderBottomNavigation(){
        mBottomNavigation(currentDestination, true, onChange = { _, value -> setState {
            val destination = value as UmReactDestination
            systemImpl.go(destination.view, destination.args,this)
        }}) {

            destinationList.filter { it.icon != null && it.view != SettingsView.VIEW_NAME }.forEach { destination ->
                destination.icon?.let {
                    mBottomNavigationAction(systemImpl.getString(destination.labelId, this),
                        mIcon(it, addAsChild = false), value = destination, showLabel = true)
                }
            }
        }
    }

    private fun RBuilder.renderSideNavigation(){
        val p: MPaperProps = jsObject { }
        p.asDynamic().style = kotlinext.js.js {
            position = "relative"; width = drawerWidth.value; display = "block"; height =
            "100%"; minHeight = "100vh"
        }

        mHidden(smDown = true, implementation = MHiddenImplementation.css) {
            mDrawer(true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = p) {
                appBarSpacer()
                themeContext.Consumer { theme ->
                    mList {
                        css {
                            backgroundColor = Color(theme.palette.background.paper)
                            width = drawerWidth
                        }
                        destinationList.filter { it.icon != null }.forEach { destination ->
                            destination.icon?.let {
                                mListItemWithIcon(it, systemImpl.getString(destination.labelId, this),
                                    divider = destination.divider , onClick = {
                                        systemImpl.go(destination.view, destination.args,this)
                                    }, selected = currentDestination == destination)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun errorFallbackComponent(text: String): ReactElement {
        // Note we purposely use a new RBuilder so we don't render into our normal display
        return RBuilder().mPaper {
            css(mainComponentErrorPaper)
            mTypography(text)
        }
    }

    private fun RBuilder.appBarSpacer() {
        themeContext.Consumer { theme ->
            styledDiv {
                css{ toolbarJsCssToPartialCss(theme.mixins.toolbar) }
            }
        }
    }

    override fun componentWillUnmount() {
        stateChangeListener = {}
    }
}

fun RBuilder.mainScreen(destination: UmReactDestination, args: Map<String,String>) = child(MainComponent::class){
    attrs.currentDestination = destination
    UstadMobileSystemImpl.instance.go(destination.view, args, this)
}