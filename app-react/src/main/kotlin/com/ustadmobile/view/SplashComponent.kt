package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.StyleManager.splashMainComponentContainer
import com.ustadmobile.util.StyleManager.splashLoadingImage
import com.ustadmobile.util.StyleManager.splashPreloadContainer
import com.ustadmobile.util.StyleManager.splashPreloadProgressBar
import com.ustadmobile.util.ThemeManager.isDarkModeActive
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


class SplashComponent (props: RProps): UstadBaseComponent<RProps, RState>(props), SplashView {

    private lateinit var mPresenter: SplashPresenter

    private var showMainComponent: Boolean = false

    override val viewName: String
        get() = SplashView.VIEW_NAME

    override fun onComponentReady() {
        mPresenter = SplashPresenter(this)
        mPresenter.onCreate()

        GlobalScope.launch(Dispatchers.Main) {
            mPresenter.handleResourceLoading()
        }
    }

    override var appName: String? = null
        set(value) {
            field = value
            document.title = value.toString()
        }

    override fun showMainComponent() {
        setState {
            showMainComponent = true
        }
    }

    override fun RBuilder.render() {
        mCssBaseline()
        themeContext.Consumer { theme ->
            ReduxAppStateManager.dispatch(ReduxThemeState(theme))
            styledDiv {
                css (splashMainComponentContainer)
                if (showMainComponent) {
                    +"Main Component"
                } else {
                    styledDiv {
                        css(splashPreloadContainer)
                        styledImg {
                            css (splashLoadingImage)
                            attrs.src = "assets/logo.png"
                        }
                        mLinearProgress {
                            css(splashPreloadProgressBar)
                            attrs.color = when {
                                isDarkModeActive() -> MLinearProgressColor.secondary
                                else -> MLinearProgressColor.primary
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
fun RBuilder.splashScreen() = child(SplashComponent::class) {}
