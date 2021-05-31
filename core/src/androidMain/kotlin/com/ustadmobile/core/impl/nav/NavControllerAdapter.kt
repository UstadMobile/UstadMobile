package com.ustadmobile.core.impl.nav

import androidx.navigation.NavController
import com.ustadmobile.core.impl.DestinationProvider

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
}