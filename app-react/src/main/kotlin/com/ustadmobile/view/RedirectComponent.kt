package com.ustadmobile.view

import com.ccfraser.muirwik.components.mCssBaseline
import com.ccfraser.muirwik.components.themeContext
import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import com.ustadmobile.util.RouteManager.findDestination
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import react.RBuilder
import react.RProps
import react.RState
import react.setState

class RedirectComponent (props: RProps): UstadBaseComponent<RProps, RState>(props), RedirectView {

    private lateinit var mPresenter: RedirectPresenter

    private var nextDestination: String? = null

    private var arguments: MutableMap<String, String> = getArgs()

    override fun componentDidMount() {
        mPresenter = RedirectPresenter(this, mapOf(ARG_WEB_PLATFORM to true.toString()),
            this, di)
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
        arguments.putAll(args)
        setState {
            arguments = when (viewName) {
                ContentEntryList2View.VIEW_NAME -> {
                    if(arguments.isEmpty() && !arguments.containsKey(ARG_PARENT_ENTRY_UID))
                        arguments[ARG_PARENT_ENTRY_UID] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
                    arguments
                }

                Login2View.VIEW_NAME -> {
                    if(!arguments.containsKey(ARG_NEXT)){
                        arguments[ARG_NEXT] = ContentEntryList2View.VIEW_NAME
                    }
                    arguments
                }
                else -> arguments
            }

            nextDestination = viewName
        }
    }


    override fun componentWillUnmount() {
        mPresenter.onDestroy()
    }
}

fun RBuilder.redirectScreen() = child(RedirectComponent::class) {}