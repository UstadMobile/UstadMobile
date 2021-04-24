package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.model.UmReactDestination
import com.ustadmobile.util.CssStyleManager.appContainer
import com.ustadmobile.util.CssStyleManager.splashComponentPreloadDiv
import com.ustadmobile.util.CssStyleManager.splashComponentCenteredImage
import com.ustadmobile.util.CssStyleManager.splashComponentProgressBar
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

interface SplashProps: RProps {
    var nextDestination: UmReactDestination

    var nextArgs: MutableMap<String,String>
}

class SplashComponent (props: SplashProps): UstadBaseComponent<SplashProps, RState>(props), SplashView {

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
        themeContext.Consumer { _ ->
            styledDiv {
                css (appContainer)
                if (showMainComponent) {
                    mainScreen(props.nextDestination, props.nextArgs)
                } else {
                    styledDiv {
                        css{ +splashComponentPreloadDiv }
                        styledImg {
                            css (splashComponentCenteredImage)
                            attrs{
                                src = "assets/${if(isDarkModeEnabled()) "logo.png" else "logo.png"}"
                            }
                        }
                        mLinearProgress {
                            css(splashComponentProgressBar)
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
fun RBuilder.splashScreen(destination: UmReactDestination, args: Map<String,String>) = child(SplashComponent::class) {
    attrs.nextDestination = destination
    attrs.nextArgs = args.toMutableMap()
}
