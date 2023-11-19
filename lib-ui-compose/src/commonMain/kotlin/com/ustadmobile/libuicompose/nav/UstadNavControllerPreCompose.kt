package com.ustadmobile.libuicompose.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.ext.toUrlQueryString
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libuicompose.util.ext.ustadDestName
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo


class UstadNavControllerPreCompose(
    private val navigator: Navigator,
    private val onPopBack: (PopNavCommand) -> Unit,
) : UstadNavController{

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    @Volatile
    private var lastNavCommandTime = systemTimeInMillis()

    private fun String.withQueryParams(
        map: Map<String, String>
    ) : String{
        return if(map.isNotEmpty()) {
            "$this?${map.toUrlQueryString()}"
        }else {
            this
        }
    }

    /**
     * onCollectNavCommand will avoid replaying a navigation command. This can happen when a viewmodel
     * has been retained (eg. user goes back) and it previously issued a navigation command on its
     * flow.
     */
    fun onCollectNavCommand(navCommand: NavCommand) {
        //Avoid replaying a navigation command
        if(navCommand.timestamp <= lastNavCommandTime)
            return

        lastNavCommandTime = navCommand.timestamp

        when(navCommand) {
            is NavigateNavCommand -> {
                Napier.d { "NavCommandEffect: Navigate: Go to ${navCommand.viewName}" }
                navigate(
                    viewName = navCommand.viewName,
                    args = navCommand.args,
                    goOptions = navCommand.goOptions
                )
            }
            is PopNavCommand -> {
                Napier.d { "NavCommandEffect: Navigate: Pop command"}
                popBackStack(
                    viewName = navCommand.viewName,
                    inclusive = navCommand.inclusive
                )
            }
            else -> {
                //do nothing
            }
        }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        if(viewName == CURRENT_DEST && inclusive) {
            //This is a simple go back one, no need to use the effect.
            navigator.goBack()
        }else {
            scope.launch {
                val currentDest = navigator.currentEntry.first()
                if(currentDest?.ustadDestName == viewName && inclusive) {
                    navigator.goBack()
                }else {
                    onPopBack(PopNavCommand(viewName, inclusive))
                }
            }
        }
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        if(goOptions.popUpToViewName == CURRENT_DEST) {
            scope.launch {
                val currentDestName = navigator.currentEntry.first()?.ustadDestName
                navigateInternal(viewName, args, goOptions.copy(
                    popUpToViewName = currentDestName
                ))
            }
        }else {
            navigateInternal(viewName, args, goOptions)
        }
    }

    private fun navigateInternal(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        val popUpToViewName = goOptions.popUpToViewName

        navigator.navigate(
            route = "/${viewName.withQueryParams(args)}",
            options = NavOptions(
                popUpTo = when {
                    goOptions.clearStack -> PopUpTo.First(inclusive = true)
                    popUpToViewName != null -> {
                        PopUpTo(
                            route = "/$popUpToViewName",
                            inclusive = goOptions.popUpToInclusive,
                        )
                    }
                    else -> PopUpTo.None
                }
            )
        )
    }

}