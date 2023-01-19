package com.ustadmobile.core.hooks

import com.ustadmobile.core.impl.nav.CommandFlowUstadNavController
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.ext.toUrlQueryString
import react.router.useNavigate

fun useNavControllerEffect(navController: CommandFlowUstadNavController): UstadNavController {
    val navigateFn = useNavigate()

    useLaunchedEffect(dependencies = arrayOf(navController)) {
        navController.commandFlow.collect {
            when {
                it is NavigateNavCommand -> {

                    //TODO: If popupto is set, action that first.
                    navigateFn("${it.viewName}?${it.args.toUrlQueryString()}")
                }
                else -> {
                    //do nothing
                }
            }
        }
    }

    return navController
}