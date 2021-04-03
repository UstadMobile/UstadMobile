package com.ustadmobile.view

import com.ustadmobile.controller.MainPresenter
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.down
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.props.MainProps
import com.ustadmobile.state.MainState
import com.ustadmobile.util.Constants.appName
import com.ustadmobile.util.Constants.drawerWidth
import com.ustadmobile.util.Constants.fullWidth
import com.ustadmobile.util.Constants.placeHolderImage
import com.ustadmobile.util.Constants.zeroPx
import com.ustadmobile.util.UmStyles.rootDiv
import kotlinext.js.jsObject
import kotlinx.css.*
import react.RBuilder
import react.router.dom.hashRouter
import react.router.dom.switch
import react.setState
import styled.StyleSheet
import styled.css
import styled.styledDiv

class MainComponent(props: MainProps): UmBaseComponent<MainProps, MainState>(props), MainView{

    private lateinit var mPresenter: MainPresenter

    override fun componentDidMount() {
        console.log("Component mounted")
        mPresenter = MainPresenter()
    }

    override fun MainState.init(props: MainProps) {
        responsiveDrawerOpen = false
        currentView = ""
    }

    override fun RBuilder.render() {
        mCssBaseline()

        themeContext.Consumer { theme ->
            styledDiv {
                css {
                    flexGrow = 1.0
                    width = 100.pct
                    zIndex = 1
                    overflow = Overflow.hidden
                    position = Position.relative
                    display = Display.flex
                    backgroundColor = Color(theme.palette.background.paper)
                }

                styledDiv {

                    css { +rootDiv }

                    mAppBar(position = MAppBarPosition.absolute) {
                        css {
                            position = Position.absolute
                            marginLeft = if(state.isRTLSupport) 0.px else drawerWidth
                            media(theme.breakpoints.up(Breakpoint.md)) {
                                width = fullWidth - if(state.isRTLSupport) zeroPx else drawerWidth
                            }
                        }

                        mToolbar {
                            mHidden(mdUp = true, implementation = MHiddenImplementation.css) {
                                mIconButton("menu", color = MColor.inherit, onClick = {
                                    //have handle click here
                                })
                            }

                            mToolbarTitle(state.currentView)

                            mAvatar {
                                attrs{
                                    src = placeHolderImage
                                    sizes = "large"
                                }
                                +"${appName.subSequence(0,1)}"
                            }
                        }
                    }

                    val p: MPaperProps = jsObject { }
                    p.asDynamic().style = kotlinext.js.js {
                        position = "relative"; width = drawerWidth.value; display = "block"; height =
                        "100%"; minHeight = "100vh"
                    }
                    mHidden(mdUp = true) {
                        mDrawer(state.responsiveDrawerOpen, MDrawerAnchor.left, MDrawerVariant.temporary, paperProps = p,
                            onClose = { setState { responsiveDrawerOpen = !responsiveDrawerOpen }}) {
                            appBarSpacer()
                            //drawerItems()
                        }
                    }
                    mHidden(smDown = true, implementation = MHiddenImplementation.css) {
                        mDrawer(true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = p) {
                            appBarSpacer()
                            //drawerItems()
                        }
                    }

                    // Main content area
                    styledDiv {
                        css {
                            height = LinearDimension.fillAvailable
                            flexGrow = 1.0
                            minWidth = 0.px
                            backgroundColor = Color(theme.palette.background.default)
                        }
                        appBarSpacer()
                        styledDiv {
                            css {
                                media(theme.breakpoints.down(Breakpoint.sm)) {
                                    height = 100.vh - 57.px
                                }
                                media(theme.breakpoints.up(Breakpoint.sm)) {
                                    height = 100.vh - 65.px
                                }

                                overflowY = Overflow.auto
                                padding(2.spacingUnits)
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            hashRouter {
                                switch{
                                    //route("/", ContentComponent::class, exact = true)
                                    //route("/Schools", SchoolsComponent::class, exact = true)
                                    //route("/Content", ContentComponent::class, exact = true)
                                    //route("/ContentDetails", ContentDetailsComponent::class, exact = true)
                                    //route("/ContentEdit", ContentEditComponent::class, exact = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*private fun RBuilder.drawerItems(fullWidth: Boolean = false) {
        themeContext.Consumer { theme ->
            mList {
                css {
                    backgroundColor = Color(theme.palette.background.paper)
                    width = if (fullWidth) LinearDimension.auto else drawerWidth
                }

                listOf(
                    MenuItem("library_books","content"),
                    MenuItem("school","schools"),
                    MenuItem("format_textdirection_r_to_l","direction"),
                    MenuItem("language","language"),
                    MenuItem("lightbulb","theme")
                ).forEachIndexed { index, item ->
                    mListItemWithIcon(item.icon, state.translations[item.label], divider = index == 2, onClick = {
                        if(index < 2){
                            setState {
                                currentView = translations[item.label]
                            }
                            val sub = item.label.subSequence(0,1).toString()
                            systemImpl.go(item.label.replaceFirst(sub, sub.toUpperCase()))
                        }else {
                            setState{
                                when (index) {
                                    2 -> {
                                        isRTLSupport = !isRTLSupport
                                        document.getElementById("root")?.setAttribute("dir",if(isRTLSupport) "rtl" else "ltr")
                                    }
                                    3 -> {
                                        currentLocale = if(currentLocale == "en-US") "sw-Tz" else "en-US"
                                        translations = AppUtils.getTranslations(currentLocale)
                                    }
                                    else -> {
                                        mPresenter.handleThemeChange()
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }*/

    private fun RBuilder.appBarSpacer() {
        themeContext.Consumer { theme ->
            val themeStyles = object : StyleSheet("ComponentStyles", isStatic = true) {
                val toolbar by css {
                    toolbarJsCssToPartialCss(theme.mixins.toolbar)
                }
            }
            styledDiv {
                css(themeStyles.toolbar)
            }
            mDivider {  }
        }
    }

    override fun updateDrawerState() {
        setState {
            responsiveDrawerOpen = !responsiveDrawerOpen
        }
    }


    override fun onThemeChange() {
        props.onThemeChange()
    }

}

fun RBuilder.initMainComponent(initialView: String, onThemeChange: () -> Unit) = child(MainComponent::class){
    attrs.initialView = initialView
    attrs.onThemeChange = onThemeChange
}