package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.*
import com.ustadmobile.core.impl.nav.NavCommand
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import react.useRequiredContext
import remix.run.router.Location


val Location.ustadViewName: String
    get() = pathname.removePrefix("/").trim()

/**
 * Collect a flow of navigation commands (e.g. from a ViewModel) and action them. If a popuptoview
 * is required, then save the destination into the
 */
fun useNavControllerEffect(commandFlow: Flow<NavCommand>?) {
    val navHostFn = useRequiredContext(NavHostContext)

    useLaunchedEffect(commandFlow) {
        commandFlow?.collect {
            Napier.d("useNavControllerEffect: collect command: $it")
            navHostFn(it)
        }
    }

}