package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.util.StyleManager.splashComponentContainer
import com.ustadmobile.util.StyleManager.splashComponentLoadingImage
import com.ustadmobile.util.StyleManager.splashComponentPreloadContainer
import com.ustadmobile.util.StyleManager.splashComponentPreloadProgressBar
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import kotlinx.browser.document
import kotlinx.coroutines.Runnable
import react.*
import styled.css
import styled.styledDiv
import styled.styledImg

class SplashComponent (props: RProps): RComponent<RProps, RState>(props), SplashView {

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
        mCssBaseline()
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
        mPresenter?.onDestroy()
        mPresenter = null
    }
}
fun RBuilder.splashComponent() = child(SplashComponent::class) {}
