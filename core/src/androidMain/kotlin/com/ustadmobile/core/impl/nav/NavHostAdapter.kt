package com.ustadmobile.core.impl.nav

import androidx.navigation.NavHostController
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.door.ext.toUrlQueryString

class NavHostAdapter(
    private val navHostController: NavHostController,
) : UstadNavController{

    fun String.withQueryParams(queryParams: Map<String, String>): String {
        if(queryParams.isNotEmpty()) {
            return "$this?${queryParams.toUrlQueryString()}"
        }else {
            return this
        }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        navHostController.popBackStack(viewName, inclusive)
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        navHostController.navigate(
            route = viewName.withQueryParams(args)
        ) {
            goOptions.popUpToViewName?.also {
                this.popUpTo(it) { inclusive = goOptions.popUpToInclusive }
            }
        }
    }

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = TODO("Not yet implemented")

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        TODO("Not yet implemented")
    }


}