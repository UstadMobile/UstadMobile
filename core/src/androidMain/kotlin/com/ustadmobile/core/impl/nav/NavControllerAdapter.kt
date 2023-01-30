package com.ustadmobile.core.impl.nav

import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.ext.toBundleWithNullableValues
import com.ustadmobile.core.view.UstadView

class NavControllerAdapter(
    private val droidNavController: NavController,
    private val destinationProvider: DestinationProvider
) : UstadNavController{

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = droidNavController.currentBackStackEntry?.let {
            val viewName = destinationProvider.lookupViewNameById(it.destination.id)
                ?: throw IllegalArgumentException("Current backstack view name not found for " +
                        "${it.destination}!")
            BackStackEntryAdapter(it, viewName)
        }

    private fun resolveViewId(
        viewName: String,
    ) : Int {
        return when(viewName) {
            UstadView.CURRENT_DEST -> droidNavController.currentDestination?.id
            UstadView.ROOT_DEST -> droidNavController.graph.startDestinationId
            else -> destinationProvider.lookupDestinationName(viewName)?.destinationId
        } ?: throw IllegalArgumentException("Could not find destination id for $viewName")
    }


    override fun getBackStackEntry(viewName: String): UstadBackStackEntry {
        return BackStackEntryAdapter(
            droidNavController.getBackStackEntry(resolveViewId(viewName)), viewName)
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        droidNavController.popBackStack(resolveViewId(viewName), inclusive)
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        droidNavController.navigate(resolveViewId(viewName),
            args.toBundleWithNullableValues(),
            navOptions {
                if(goOptions.clearStack) {
                    popUpTo(droidNavController.graph.startDestinationId) {
                        inclusive = false
                    }
                }else if(goOptions.popUpToViewName != null) {
                    popUpTo(resolveViewId(viewName)) {
                        inclusive = goOptions.popUpToInclusive
                    }
                }
            }
        )
    }
}