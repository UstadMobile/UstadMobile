package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.KEY_NAV_CONTROLLER_CLEAR_STACK
import com.ustadmobile.core.components.KEY_NAV_CONTROLLER_NAVTO_AFTER_POP
import com.ustadmobile.core.components.KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE
import com.ustadmobile.core.components.KEY_NAV_CONTROLLER_POPUPTO_PAGE
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.door.ext.toUrlQueryString
import history.Location
import kotlinx.browser.sessionStorage
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.set
import react.router.useNavigate


val Location.ustadViewName: String
    get() = pathname.removePrefix("/").trim()

/**
 * Collect a flow of navigation commands (e.g. from a ViewModel) and action them. If a popuptoview
 * is required, then save the destination into the
 */
fun useNavControllerEffect(commandFlow: Flow<NavCommand>?) {
    val navigateFn = useNavigate()


    useLaunchedEffect(dependencies = arrayOf(commandFlow)) {
        commandFlow?.collect {
            when(it) {
                is NavigateNavCommand -> {
                    val popUpToView = it.goOptions.popUpToViewName

                    if(popUpToView == null && !it.goOptions.clearStack) {
                        console.log("useNavControllerEffect: go to /${it.viewName}?${it.args.toUrlQueryString()}")
                        navigateFn("/${it.viewName}?${it.args.toUrlQueryString()}")
                    }else {
                        console.log("useNavControllerEffect: pop, then go /${it.viewName}?${it.args.toUrlQueryString()}")
                        if(popUpToView != null) {
                            sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE] = popUpToView
                        }
                        sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] = it.goOptions.clearStack.toString()
                        sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE] = it.goOptions.popUpToInclusive.toString()
                        sessionStorage[KEY_NAV_CONTROLLER_NAVTO_AFTER_POP] = "/${it.viewName}?${it.args.toUrlQueryString()}"
                        navigateFn(-1)
                    }

                }
                else -> {
                    //do nothing
                }
            }
        }
    }

}