package com.ustadmobile.libuicompose.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.ext.toUrlQueryString
import com.ustadmobile.libuicompose.util.ext.ustadDestName
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

    private fun String.withQueryParams(
        map: Map<String, String>
    ) : String{
        return if(map.isNotEmpty()) {
            "$this?${map.toUrlQueryString()}"
        }else {
            this
        }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        onPopBack(PopNavCommand(viewName, inclusive))
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

    fun navigateInternal(
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