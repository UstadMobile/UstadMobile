package com.ustadmobile.libuicompose.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.door.ext.toUrlQueryString
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo


class UstadNavControllerPreCompose(
    private val navigator: Navigator,
    private val onPopBack: (PopNavCommand) -> Unit,
) : UstadNavController{

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
        val popUpToViewName = goOptions.popUpToViewName

        navigator.navigate(
            route = "/${viewName.withQueryParams(args)}",
            options = NavOptions(
                popUpTo = when {
                    goOptions.clearStack -> PopUpTo.First(inclusive = true)
                    popUpToViewName != null -> {
                        PopUpTo(
                            route = popUpToViewName,
                            inclusive = goOptions.popUpToInclusive,
                        )
                    }
                    else -> PopUpTo.None
                }
            )
        )
    }
}