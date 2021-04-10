package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mFab
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.input.mInput
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.list.mListItemWithIcon
import com.ccfraser.muirwik.components.styles.Breakpoint
import com.ccfraser.muirwik.components.styles.down
import com.ccfraser.muirwik.components.styles.up
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.model.statemanager.UmAppState
import com.ustadmobile.util.Constants.drawerWidth
import com.ustadmobile.util.Constants.fullWidth
import com.ustadmobile.util.Constants.placeHolderImage
import com.ustadmobile.util.Constants.zeroPx
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmRouting.destinationList
import com.ustadmobile.util.UmRouting.findDestination
import com.ustadmobile.util.UmRouting.umRouter
import com.ustadmobile.util.UmStyles
import com.ustadmobile.util.UmStyles.appContainer
import com.ustadmobile.util.UmStyles.fab
import com.ustadmobile.util.UmStyles.mainComponentContentArea
import com.ustadmobile.util.UmStyles.mainComponentRootDiv
import com.ustadmobile.util.UmStyles.mainComponentSearch
import com.ustadmobile.util.UmStyles.mainComponentSearchIcon
import kotlinext.js.jsObject
import kotlinx.css.*
import org.kodein.di.instance
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.StyleSheet
import styled.css
import styled.styledDiv

interface MainProps: RProps {
    var initialView: String
}


class MainComponent(props: MainProps): UmBaseComponent<MainProps, RState>(props){

    private var activeAccount: UmAccount? = null

    private var currentTile: String = ""

    private var responsiveDrawerOpen: Boolean = false

    private var isRTLSupport: Boolean = false

    private var showSearch: Boolean = false

    private var showFab: Boolean = false

    private var fabIcon: String = "edit"

    private var fabLabel: String = ""

    private val impl : UstadMobileSystemImpl by instance()


    override fun componentWillMount() {
        val dest = findDestination(props.initialView)
        setState {
            currentTile = dest?.let { impl.getString(it.labelId, this) }.toString()
            responsiveDrawerOpen = true
            showSearch = dest?.showSearch?:false
        }
        StateManager.subscribe(stateChangeListener)
    }

    override fun RBuilder.render() {
        mCssBaseline()

        themeContext.Consumer { theme ->
            styledDiv {
                css {
                    +appContainer
                    backgroundColor = Color(theme.palette.background.paper)
                }

                styledDiv {
                    css { +mainComponentRootDiv }

                    /*//Loading indicator
                    mLinearProgress {
                        css(progressIndicator)
                        attrs {
                            color = if(UmReactUtil.isDarkModeEnabled()) MLinearProgressColor.secondary
                            else MLinearProgressColor.primary
                        }
                    }*/
                    mAppBar(position = MAppBarPosition.absolute) {
                        css {
                            position = Position.absolute
                            marginLeft = if(isRTLSupport) 0.px else drawerWidth
                            media(theme.breakpoints.up(Breakpoint.md)) {
                                width = fullWidth - if(isRTLSupport) zeroPx else drawerWidth
                            }
                        }

                        mToolbar {
                            mHidden(mdUp = true, implementation = MHiddenImplementation.css) {
                                mIconButton("menu", color = MColor.inherit, onClick = {
                                    setState {
                                        responsiveDrawerOpen = !responsiveDrawerOpen
                                    }
                                })
                            }

                            mToolbarTitle(currentTile)


                           if(showSearch){
                               styledDiv {
                                   css(mainComponentSearch)
                                   styledDiv {
                                       css(mainComponentSearchIcon)
                                       mIcon("search")
                                   }
                                   val inputProps = object: RProps {
                                       val className = "${UmStyles.name}-mainComponentInputSearch"
                                   }
                                   mInput(placeholder = "Search...", disableUnderline = true) {
                                       attrs.inputProps = inputProps
                                       css {
                                           color = Color.inherit
                                       }
                                   }
                               }
                           }

                            mAvatar {
                                attrs{
                                    src = placeHolderImage
                                    sizes = "large"
                                }
                                +"${activeAccount?.firstName?.first()}"
                            }
                        }
                    }

                    val p: MPaperProps = jsObject { }
                    p.asDynamic().style = kotlinext.js.js {
                        position = "relative"; width = drawerWidth.value; display = "block"; height =
                        "100%"; minHeight = "100vh"
                    }
                    mHidden(mdUp = true) {
                        mDrawer(responsiveDrawerOpen, MDrawerAnchor.left, MDrawerVariant.temporary, paperProps = p,
                            onClose = { setState { responsiveDrawerOpen = !responsiveDrawerOpen }}) {
                            appBarSpacer()
                            drawerItems()
                        }
                    }

                    mHidden(smDown = true, implementation = MHiddenImplementation.css) {
                        mDrawer(true, MDrawerAnchor.left, MDrawerVariant.permanent, paperProps = p) {
                            appBarSpacer()
                            drawerItems()
                        }
                    }

                    // Main content area, this div holds the contents
                    styledDiv {
                        css {
                            +mainComponentContentArea
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
                            umRouter()
                        }
                    }

                    if(showFab){
                        mFab(fabIcon, fabLabel.toUpperCase(), color = MColor.secondary) {
                            css(fab)
                        }
                    }
                }

            }
        }
    }

    private val stateChangeListener : (UmAppState) -> Unit = {
        setState {
            showFab = it.state.showFab
            fabLabel = impl.getString(if(it.state.isDetailScreen) MessageID.edit else MessageID.content , this)
            fabIcon = if(it.state.isDetailScreen) "edit" else "add"
        }
    }

    private fun RBuilder.drawerItems(fullWidth: Boolean = false) {
        themeContext.Consumer { theme ->
            mList {
                css {
                    backgroundColor = Color(theme.palette.background.paper)
                    width = if (fullWidth) LinearDimension.auto else drawerWidth
                }

                destinationList.filter { it.icon != null }.forEach { item ->
                    item.icon?.let {
                        mListItemWithIcon(it, impl.getString(item.labelId, this), divider = item.divider , onClick = {
                            setState {
                                showSearch = item.showSearch
                                currentTile = impl.getString(item.labelId, this)
                            }
                            impl.go(item.view, mapOf(),this)
                        })
                    }
                }
            }
        }
    }

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
}

fun RBuilder.initMainComponent(initialView: String) = child(MainComponent::class){
    attrs.initialView = initialView
}