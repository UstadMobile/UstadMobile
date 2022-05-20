package com.ustadmobile.core.impl.nav

import androidx.navigation.NavController
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
                ?: throw IllegalArgumentException("Current backstack viewname not found for " +
                        "${it.destination}!")
            BackStackEntryAdapter(it, viewName)
        }

    private fun lookupDestinationName(viewName: String): Int? {
        return when(viewName) {
            UstadView.CURRENT_DEST -> droidNavController.currentDestination?.id
            UstadView.ROOT_DEST -> droidNavController.graph.startDestinationId
            else -> destinationProvider.lookupDestinationName(viewName)?.destinationId
        }
    }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        val destinationId = lookupDestinationName(viewName) ?: return null

        return BackStackEntryAdapter(
            droidNavController.getBackStackEntry(destinationId), viewName)
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        val popViewId = lookupDestinationName(viewName) ?: return

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