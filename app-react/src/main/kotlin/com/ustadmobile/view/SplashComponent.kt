package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.util.UmStyles.appContainer
import com.ustadmobile.util.UmStyles.preloadComponentCenteredDiv
import com.ustadmobile.util.UmStyles.preloadComponentCenteredImage
import com.ustadmobile.util.UmStyles.preloadComponentProgressBar
import com.ustadmobile.util.UmUtil.isDarkModeEnabled
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

class UmAppComponent (props: RProps): UmBaseComponent<RProps, RState>(props), SplashView {

    private lateinit var mPresenter: SplashPresenter

    private var showMainComponent: Boolean = false

    override fun componentWillMount() {
        mPresenter = SplashPresenter(this)
        GlobalScope.launch(Dispatchers.Main) {
            mPresenter.handleResourceLoading()
        }
    }


    override fun RBuilder.render() {
        mCssBaseline()
        themeContext.Consumer { theme ->
            styledDiv {
                css {
                    +appContainer
                }

                if (showMainComponent) {
                    initMainComponent("Content")
                } else {
                    styledDiv {
                        css { +preloadComponentCenteredDiv }
                        styledImg {
                            css {+preloadComponentCenteredImage}
                            attrs{
                                src = "assets/${if(isDarkModeEnabled()) "logo.png" else "logo.png"}"
                            }
                        }
                        mLinearProgress {
                            css{+preloadComponentProgressBar}
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
}

fun RBuilder.showPreload() = child(UmAppComponent::class) {}

