package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.*
import com.ustadmobile.core.impl.nav.NavCommand
import history.Location
import kotlinx.coroutines.flow.Flow
import react.useContext


val Location.ustadViewName: String
    get() = pathname.removePrefix("/").trim()

/**
 * Collect a flow of navigation commands (e.g. from a ViewModel) and action them. If a popuptoview
 * is required, then save the destination into the
 */
fun useNavControllerEffect(commandFlow: Flow<NavCommand>?) {
    val navHostFn = useContext(NavHostContext)

    useLaunchedEffect(dependencies = arrayOf(commandFlow)) {
        commandFlow?.collect {
            navHostFn(it)
        }
    }

}