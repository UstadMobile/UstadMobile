package com.ustadmobile.view

import com.ccfraser.muirwik.components.mCssBaseline
import com.ccfraser.muirwik.components.themeContext
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.util.RouteManager.findDestination
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.RouteManager.getPathName
import com.ustadmobile.util.StateManager
import react.RBuilder
import react.RProps
import react.RState
import react.setState

class RedirectComponent (props: RProps): UstadBaseComponent<RProps, RState>(props), RedirectView {

    private lateinit var mPresenter: RedirectPresenter

    private var nextDestination: String? = null

    private var arguments = mutableMapOf<String, String>()

    override fun componentDidMount() {
        val viewName = getPathName()
        if(viewName != null){
            arguments[ARG_NEXT] = "${viewName}?${getArgs(mapOf(ARG_WEB_PLATFORM 
                    to true.toString())).toQueryString()}"
        }
        mPresenter = RedirectPresenter(this, arguments, this, di)
        mPresenter.onCreate(mapOf())
    }



    override fun RBuilder.render() {
        mCssBaseline()
        themeContext.Consumer { theme ->
            StateManager.dispatch(StateManager.UmTheme(theme))
            if(nextDestination != null){
                findDestination(nextDestination)?.let { splashScreen(it, arguments) }
            }
        }
    }

    override fun showNextScreen(viewName: String, args: Map<String, String>) {
        setState {
            arguments = args.toMutableMap()
            nextDestination = viewName
        }
    }


    override fun componentWillUnmount() {
        mPresenter.onDestroy()
    }
}

fun RBuilder.redirectScreen() = child(RedirectComponent::class) {}