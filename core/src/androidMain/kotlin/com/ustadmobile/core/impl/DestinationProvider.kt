package com.ustadmobile.core.impl

/**
 * This interface is used by our Android implementation to find the the NavController destination
 * ID for a given VIEW_NAME string, and vice-versa
 */
interface DestinationProvider {

    fun lookupDestinationName(viewName: String): UstadDestination?

    fun lookupDestinationById(destinationId: Int): UstadDestination?

    fun lookupViewNameById(destinationId: Int): String?

    val navControllerViewId: Int

}