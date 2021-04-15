package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.util.CssStyleManager.appContainer
import com.ustadmobile.util.CssStyleManager.preloadComponentCenteredDiv
import com.ustadmobile.util.CssStyleManager.preloadComponentCenteredImage
import com.ustadmobile.util.CssStyleManager.preloadComponentProgressBar
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.UmReactUtil.isDarkModeEnabled
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg

class SplashComponent (props: RProps): UmBaseComponent<RProps, RState>(props), SplashView {

    private lateinit var mPresenter: SplashPresenter

    private var showMainComponent: Boolean = false

    override fun componentDidMount() {
        mPresenter = SplashPresenter(this)
        GlobalScope.launch(Dispatchers.Main) {
            mPresenter.handleResourceLoading()
        }
    }

    override var appName: String? = null
        set(value) {
            document.title = value.toString()
            field = value
        }

    override fun showMainComponent() {
        setState {
            showMainComponent = true
        }
    }

    override fun RBuilder.render() {
        mCssBaseline()
        themeContext.Consumer { theme ->
            StateManager.dispatch(StateManager.UmTheme(theme))
            styledDiv {
                css (appContainer)
                if (showMainComponent) {
                    val mArgs =  mutableMapOf<String,String>()
                    mArgs.putAll(getArgs())
                    if(mArgs.isEmpty()){
                        mArgs[ARG_PARENT_ENTRY_UID] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
                    }
                    initMainComponent(ContentEntryList2View.VIEW_NAME, mArgs)
                } else {
                    styledDiv {
                        css(preloadComponentCenteredDiv)
                        styledImg {
                            css (preloadComponentCenteredImage)
                            attrs{
                                src = "assets/${if(isDarkModeEnabled()) "logo.png" else "logo.png"}"
                            }
                        }
                        mLinearProgress {
                            css(preloadComponentProgressBar)
                            attrs {
                                color = if(isDarkModeEnabled()) MLinearProgressColor.secondary
                                else MLinearProgressColor.primary
                            }
                        }
                    }

                }
            }
        }
    }

    override fun componentWillUnmount() {
        mPresenter.onDestroy()
    }
}

fun RBuilder.showPreload() = child(SplashComponent::class) {}

