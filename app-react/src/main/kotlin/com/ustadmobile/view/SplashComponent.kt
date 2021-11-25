package com.ustadmobile.view

import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.mui.components.UmLinearProgressColor
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umCssBaseline
import com.ustadmobile.mui.components.umLinearProgress
import kotlinx.browser.document
import kotlinx.coroutines.Runnable
import react.*
import com.ustadmobile.util.*
import com.ustadmobile.util.StyleManager.splashComponentContainer
import com.ustadmobile.util.StyleManager.splashComponentLoadingImage
import com.ustadmobile.util.StyleManager.splashComponentPreloadContainer
import com.ustadmobile.util.StyleManager.splashComponentPreloadProgressBar
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import styled.css
import styled.styledDiv
import styled.styledImg

class SplashComponent (props: UmProps): RComponent<UmProps, UmState>(props), SplashView {

    private var mPresenter: SplashPresenter? = null

    override fun componentWillMount() {
        mPresenter = SplashPresenter(this)
        mPresenter?.onCreate()
    }

    override var appName: String? = null
        set(value) {
            field = value
            document.title = value.toString()
        }

    override var loading: Boolean = true
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {}

    override fun runOnUiThread(r: Runnable?) {}

    override fun RBuilder.render() {
        umCssBaseline()
        themeContext.Consumer { _ ->

            styledDiv {
                css (splashComponentContainer)
                if (!loading) {
                    mainComponent()
                } else {
                    styledDiv {
                        css(splashComponentPreloadContainer)
                        styledImg {
                            css (splashComponentLoadingImage)
                            attrs.src = "assets/logo.png"
                        }

                        val color = when {
                            isDarkModeActive() -> UmLinearProgressColor.secondary
                            else -> UmLinearProgressColor.primary
                        }

                        umLinearProgress(color = color) {
                            css(splashComponentPreloadProgressBar)
                        }
                    }

                }
            }
        }
    }

    override fun componentWillUnmount() {
        mPresenter = null
    }
}
fun RBuilder.splashComponent() = child(SplashComponent::class) {}
