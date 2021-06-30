package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.util.StyleManager.splashComponentContainer
import com.ustadmobile.util.StyleManager.splashComponentLoadingImage
import com.ustadmobile.util.StyleManager.splashComponentPreloadContainer
import com.ustadmobile.util.StyleManager.splashComponentPreloadProgressBar
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import kotlinx.browser.document
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

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        mPresenter = SplashPresenter(this)
        mPresenter.onCreate()

        mPresenter.handleResourceLoading()
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
        themeContext.Consumer { _ ->

            styledDiv {
                css (splashComponentContainer)
                if (showMainComponent) {
                    mainComponent()
                } else {
                    styledDiv {
                        css(splashComponentPreloadContainer)
                        styledImg {
                            css (splashComponentLoadingImage)
                            attrs.src = "assets/logo.png"
                        }
                        mLinearProgress {
                            css(splashComponentPreloadProgressBar)
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
fun RBuilder.splashComponent() = child(SplashComponent::class) {}
