package com.ustadmobile.view

import com.ccfraser.muirwik.components.mCssBaseline
import com.ccfraser.muirwik.components.themeContext
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CURRENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.util.RouteManager.findDestination
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.RouteManager.getPathName
import com.ustadmobile.util.StateManager
import kotlinx.browser.window
import react.RBuilder
import react.RProps
import react.RState
import react.setState

class RedirectComponent (props: RProps): UstadBaseComponent<RProps, RState>(props), RedirectView {

    private lateinit var mPresenter: RedirectPresenter

    private var nextDestination: String? = null

    private var arguments = mutableMapOf(ARG_WEB_PLATFORM to true.toString())

    override var viewName: String? = null

    private var timeOutId = 0

    override fun onComponentReady() {
        val currentView = getPathName()
        val args = getArgs()

        //responsible for handling browser refresh
        if(currentView != null){
            arguments[ARG_CURRENT] = currentView + "?" + args.toQueryString()
        }
        timeOutId = window.setTimeout({
            handleNextDestination()
        }, 1500)
        mPresenter = RedirectPresenter(this, arguments, this, di)
        mPresenter.onCreate(mapOf())
    }

    override fun onViewChanged(newView: String?) {
        super.onViewChanged(newView)
        window.clearTimeout(timeOutId)
        handleNextDestination()
    }

    private fun handleNextDestination(){
        setState { nextDestination = getPathName()}
    }

    override fun RBuilder.render() {
        mCssBaseline()
        themeContext.Consumer { theme ->
            StateManager.dispatch(StateManager.UmTheme(theme))
            if(nextDestination != null){
                findDestination(nextDestination)?.let { splashScreen(it, getArgs().filter { entry ->
                    entry.key != ARG_WEB_PLATFORM && entry.key != ARG_CURRENT }) }
            }
        }
    }

    override fun componentWillUnmount() {
        mPresenter.onDestroy()
    }
}

fun RBuilder.redirectScreen() = child(RedirectComponent::class) {}