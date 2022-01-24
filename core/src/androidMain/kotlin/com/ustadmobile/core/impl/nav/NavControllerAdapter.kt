package com.ustadmobile.core.impl.nav

import androidx.navigation.NavController
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.ext.toBundleWithNullableValues

class NavControllerAdapter(val droidNavController: NavController,
                           val destinationProvider: DestinationProvider) : UstadNavController{

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = droidNavController.currentBackStackEntry?.let {
            val viewName = destinationProvider.lookupViewNameById(it.destination.id)
                ?: throw IllegalArgumentException("Current backstack viewname not found for ${it.destination.id}!")
            BackStackEntryAdapter(it, viewName)
        }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        val ustadDestination = destinationProvider.lookupDestinationName(viewName) ?: return null

        return BackStackEntryAdapter(
            droidNavController.getBackStackEntry(ustadDestination.destinationId), viewName)
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        val popViewId = destinationProvider.lookupDestinationName(viewName)?.destinationId
            ?: return

        droidNavController.popBackStack(popViewId, inclusive)
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        val destinationId = destinationProvider.lookupDestinationName(viewName)?.destinationId
            ?: return

        droidNavController.navigate(destinationId, args.toBundleWithNullableValues(),
            goOptions.toNavOptions(droidNavController, destinationProvider))
    }
}