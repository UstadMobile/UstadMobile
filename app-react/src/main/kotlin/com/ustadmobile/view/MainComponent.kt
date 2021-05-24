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
import com.ustadmobile.util.CssStyleManager.alignTextToStart
import com.ustadmobile.util.CssStyleManager.appContainer
import com.ustadmobile.util.CssStyleManager.bottomFixedElements
import com.ustadmobile.util.CssStyleManager.defaultFullWidth
import com.ustadmobile.util.CssStyleManager.drawerWidth
import com.ustadmobile.util.CssStyleManager.fullWidth
import com.ustadmobile.util.CssStyleManager.isMobile
import com.ustadmobile.util.CssStyleManager.mainBrandIcon
import com.ustadmobile.util.CssStyleManager.mainBrandIconContainer
import com.ustadmobile.util.CssStyleManager.mainComponentAvatarInner
import com.ustadmobile.util.CssStyleManager.mainComponentAvatarOuter
import com.ustadmobile.util.CssStyleManager.mainComponentContainer
import com.ustadmobile.util.CssStyleManager.mainComponentContentContainer
import com.ustadmobile.util.CssStyleManager.mainComponentContents
import com.ustadmobile.util.CssStyleManager.mainComponentErrorPaper
import com.ustadmobile.util.CssStyleManager.mainComponentFab
import com.ustadmobile.util.CssStyleManager.mainComponentSearch
import com.ustadmobile.util.CssStyleManager.mainComponentSearchIcon
import com.ustadmobile.util.CssStyleManager.mainToolbar
import com.ustadmobile.util.CssStyleManager.progressIndicator
import com.ustadmobile.util.CssStyleManager.tabletAndHighEnd
import com.ustadmobile.util.CssStyleManager.zeroPx
import com.ustadmobile.util.RouteManager.destinationList
import com.ustadmobile.util.RouteManager.findDestination
import com.ustadmobile.util.RouteManager.getPathName
import com.ustadmobile.util.RouteManager.renderRoutes
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmReactUtil.isDarkModeEnabled
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinext.js.jsObject
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.dom.addClass
import kotlinx.html.id
import react.*
import react.dom.div
import react.dom.span
import styled.css
import styled.styledDiv
import styled.styledImg

interface MainProps: RProps {
    var currentDestination: UmReactDestination
}

class MainComponent(props: MainProps): UstadBaseComponent<MainProps, RState>(props){

    private var activeAccount: UmAccount? = null

    private val altBuilder = RBuilder()

    override var viewName: String? = null

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

    override fun onComponentReady() {
        StateManager.subscribe(stateChangeListener)
        accountManager.activeAccountLive.observeWithLifecycleOwner(this,
            mActiveUserObserver)
        handleTitle(getPathName())
    }

    override fun onViewChanged(newView: String?) {
        super.onViewChanged(newView)
        handleTitle(newView)
    }

    private fun handleTitle(newView: String?){
        val destination = findDestination(newView)
        if(destination != null){
            if(destination.labelId != 0 && destination.labelId != MessageID.content){
                title = getString(destination.labelId)
            }
            setState {
                currentDestination = destination
            }
        }
    }


    override fun RBuilder.render() {
        errorBoundary(errorFallbackComponent(getString(MessageID.error))){
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
                                val removeLeftMargin = isMobile or isRTLSupported or !currentDestination.showNavigation
                                position = Position.absolute
                                marginLeft = if(removeLeftMargin) zeroPx else drawerWidth.px
                                media(theme.breakpoints.up(Breakpoint.md)) {
                                    width = fullWidth - if(removeLeftMargin) zeroPx else drawerWidth.px
                                }
                                if(currentDestination.hasTabs){
                                    paddingBottom = 0.px
                                    marginBottom = 0.px
                                }
                            }

                            mToolbar {
                                attrs.asDynamic().id = "um-toolbar"
                                css{ +mainToolbar }

                                umGridContainer {
                                    umItem(MGridSize.cells5){
                                        css{marginTop = 4.px}
                                        mToolbarTitle(globalState.title?:"")
                                    }

                                    umItem(MGridSize.cells5, MGridSize.cells6){
                                        styledDiv {
                                            css{
                                                +mainComponentSearch
                                                display = Display.none
                                                media(theme.breakpoints.up(tabletAndHighEnd)){
                                                    display = if(currentDestination.showSearch)
                                                        Display.block else Display.none
                                                }
                                            }
                                            styledDiv {
                                                css(mainComponentSearchIcon)
                                                mIcon("search")
                                            }
                                            val inputProps = object: RProps {
                                                val className = "${CssStyleManager.name}-mainComponentInputSearchClass"
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
                                                display = if(currentDestination.showNavigation) Display.block else Display.none
                                                +mainComponentAvatarOuter
                                            }

                                            attrs {
                                                onClick = {}
                                            }

                                            mAvatar{
                                                css (mainComponentAvatarInner)
                                                mTypography("${activeAccount?.firstName?.first()}",
                                                    align = MTypographyAlign.center,
                                                    variant = MTypographyVariant.h5){
                                                    css{ marginTop = (1.5).px }
                                                }
                                            }
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

                        if(currentDestination.showNavigation){
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
                                attrs { id = "main-content" }
                                css (mainComponentContents)
                                renderRoutes()
                            }
                        }

                        styledDiv {
                            css{ +bottomFixedElements }
                            if(globalState.showFab){
                                mFab(globalState.fabIcon, globalState.fabLabel.toUpperCase(), color = MColor.secondary,
                                    onClick = globalState.onFabClicked) {
                                    css{
                                        display = if(globalState.showFab) Display.flex else Display.none
                                        +mainComponentFab
                                    }
                                }
                            }

                            if(currentDestination.showNavigation){
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
       mHidden(mdUp = true) {
           mBottomNavigation(currentDestination, true, onChange = { _, value -> setState {
               val destination = value as UmReactDestination
               systemImpl.go(destination.view, destination.args,this)
           }}) {

               destinationList.filter { it.icon != null && it.view != SettingsView.VIEW_NAME }.forEach { destination ->
                   destination.icon?.let {
                       mBottomNavigationAction(getString(destination.labelId),
                           mIcon(it, addAsChild = false), value = destination, showLabel = true)
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

        mHidden(smDown = true) {
            mDrawer(true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = p) {
                styledDiv {
                    css(mainBrandIconContainer)
                    styledImg(src = "assets/brand-logo.png"){css(mainBrandIcon)}
                }
                mDivider { css(defaultFullWidth) }
                themeContext.Consumer { theme ->
                    mList {
                        css {
                            backgroundColor = Color(theme.palette.background.paper)
                            width = drawerWidth.px
                        }
                        destinationList.filter { it.icon != null }.forEach { destination ->
                            destination.icon?.let {
                                mListItemWithIcon(it, getString(destination.labelId),
                                    divider = destination.divider , onClick = {
                                        systemImpl.go(destination.view, destination.args,this)
                                    }, selected = currentDestination == destination){
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