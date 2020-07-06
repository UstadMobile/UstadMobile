package com.ustadmobile.core.impl

interface DestinationProvider {

    fun lookupDestinationName(viewName: String): UstadDestination?

    fun lookupDestinationById(destinationId: Int): UstadDestination?

    val navControllerViewId: Int

}