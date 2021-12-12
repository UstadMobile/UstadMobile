package com.ustadmobile.view

import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.mui.components.LinearProgressColor
import com.ustadmobile.mui.components.themeContext
import com.ustadmobile.mui.components.umCssBaseline
import com.ustadmobile.mui.components.umLinearProgress
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.splashComponentContainer
import com.ustadmobile.util.StyleManager.splashComponentLoadingImage
import com.ustadmobile.util.StyleManager.splashComponentPreloadContainer
import com.ustadmobile.util.StyleManager.splashComponentPreloadProgressBar
import com.ustadmobile.util.ThemeManager.isDarkModeActive
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umPartner
import kotlinx.browser.document
import kotlinx.coroutines.Runnable
import react.RBuilder
import react.RComponent
import react.setState
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
                    renderMainComponent()
                } else {
                    styledDiv {
                        css(splashComponentPreloadContainer)
                        styledImg {
                            css (splashComponentLoadingImage)
                            attrs.src = "assets/logo_blue.png"
                        }

                        val color = when {
                            isDarkModeActive() -> LinearProgressColor.secondary
                            else -> LinearProgressColor.primary
                        }

                        umLinearProgress(color = color) {
                            css(splashComponentPreloadProgressBar)
                        }
                    }

                    styledDiv {
                        css(StyleManager.partnersList)
                        umGridContainer {
                            umPartner("tajik_emblem.webp")
                            umPartner("unicef_tj.webp")
                            umPartner("Eu_logo_tg.webp")
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
fun RBuilder.renderSplashComponent() = child(SplashComponent::class) {}
