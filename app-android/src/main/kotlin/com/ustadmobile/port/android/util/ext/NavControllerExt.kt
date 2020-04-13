package com.ustadmobile.port.android.util.ext

import android.os.Bundle
import androidx.navigation.NavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView

fun NavController.currentBackStackEntrySavedStateMap() = this.currentBackStackEntry?.savedStateHandle?.toStringMap()

/**
 * Navigate to a list view in picker mode for the given entity type and destination id
 *
 * @param entityClass The class for the entity type that is going to be selected
 * @param destinationId The destination id as per the navigation map
 * @param args optional additional args to pass to the list view
 * @param destinationResultKey the key to use in the SavedStateHandle
 */
fun NavController.navigateToPickEntityFromList(entityClass: Class<*>, destinationId: Int,
                                               args: Bundle = Bundle(),
                                               destinationResultKey: String = entityClass.simpleName){
    val currentBackStateEntryVal = currentBackStackEntry
    if(currentBackStateEntryVal != null)
        args.putResultDestInfo(currentBackStateEntryVal, destinationResultKey)

    args.putString(UstadView.ARG_LISTMODE, ListViewMode.PICKER.toString())
    navigate(destinationId, args)
}

/**
 * Navigate to an edit view and instruct the destination to save the result to the back stack
 *
 * @entity the entity to be edited (null to create a new entity)
 * @param entityClass The class for the entity type that is going to be selected
 * @param destinationId The destination id as per the navigation map
 */
fun <T> NavController.navigateToEditEntity(entity: T?, destinationId: Int, entityClass: Class<T>,
                                           destinationResultKey: String = entityClass.simpleName) {
    val argBundle = Bundle()
    val backStateEntryVal = currentBackStackEntry

    if(backStateEntryVal != null) {
        argBundle.putResultDestInfo(backStateEntryVal, destinationResultKey)
    }

    if(entity != null)
        argBundle.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, entity)

    navigate(destinationId, argBundle)
}