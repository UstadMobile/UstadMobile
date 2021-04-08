package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ustadmobile.controller.SplashPresenter
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.util.UmReactUtil.isDarkModeEnabled
import com.ustadmobile.util.UmStyles.appContainer
import com.ustadmobile.util.UmStyles.preloadComponentCenteredDiv
import com.ustadmobile.util.UmStyles.preloadComponentCenteredImage
import com.ustadmobile.util.UmStyles.preloadComponentProgressBar
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.*
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

    private val diModule = DI.Module("UstadApp-React"){

        bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

        bind<UstadAccountManager>() with singleton {
            UstadAccountManager(instance(), this, di)
        }

        constant(UstadMobileSystemCommon.TAG_DOWNLOAD_ENABLED) with true

        bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton { Dispatchers.Main }

        bind<ContentEntryOpener>() with scoped(EndpointScope.Default).singleton {
            ContentEntryOpener(di, context)
        }
    }

    override fun componentDidMount() {
        val umDi = DI.lazy { import(diModule)}
        js("window.di = umDi")
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
            js("window.theme = theme")
            styledDiv {
                css {
                    +appContainer
                }

                if (showMainComponent) {
                    initMainComponent(ContentEntryList2View.VIEW_NAME)
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
}

fun RBuilder.showPreload() = child(SplashComponent::class) {}

