package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.props.UmAppProps
import com.ustadmobile.state.UmAppState
import com.ustadmobile.util.UmStyles.appContainer
import com.ustadmobile.util.UmStyles.preloadComponentCenteredDiv
import com.ustadmobile.util.UmStyles.preloadComponentCenteredImage
import com.ustadmobile.util.UmStyles.preloadComponentProgressBar
import com.ustadmobile.util.UmUtil.isDarkModeEnabled
import kotlinx.browser.window
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import styled.styledImg

class UmAppComponent (props: UmAppProps): UmBaseComponent<UmAppProps, UmAppState>(props) {

    private var showMainComponent: Boolean = false

    private var timerId = 0

    override fun componentWillMount() {
        timerId = window.setInterval({
            setState {
                showMainComponent = true
            }
        }, 4000)
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
        window.clearInterval(timerId)
    }
}

fun RBuilder.showPreload() = child(UmAppComponent::class) {}

