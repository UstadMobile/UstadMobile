package com.ustadmobile.port.android.view.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.view.UstadBaseFragment

/**
 * Set the title of an edit fragment to "New Widget" or "Edit Widget" based on whether or not a new
 * entity is being created.
 *
 * @param entityTitleId the string id for the entity type (e.g. Widget)
 */
fun Fragment.setEditFragmentTitle(entityTitleId: Int) {
    val entityUid = arguments?.getString(UstadView.ARG_ENTITY_UID)?.toLong() ?: 0L
    val entityJsonStr = arguments?.getString(UstadEditView.ARG_ENTITY_JSON)
    (activity as? AppCompatActivity)?.supportActionBar?.title = if(entityUid != 0L || entityJsonStr != null){
         getString(R.string.edit_entity, getString(entityTitleId))
    }else {
        getString(R.string.new_entity, getString(entityTitleId))
    }
}

/**
 * Save the result of a fragment (e.g. a selection from a list or newly created entity) to the
 * BackStack SavedStateHandle as specified by ARG_RESULT_DEST_ID and ARG_RESULT_DEST_KEY
 */
fun Fragment.saveResultToBackStackSavedStateHandle(result: List<*>) {
    val saveToDestination = arguments?.getString(UstadBaseFragment.ARG_RESULT_DEST_ID)
    val saveToKey = arguments?.getString(UstadBaseFragment.ARG_RESULT_DEST_KEY)
    val navController = findNavController()
    if(saveToDestination != null && saveToKey != null) {
        val saveToDestId = saveToDestination.toInt()
        val destStackEntry = navController.getBackStackEntry(saveToDestId)
        destStackEntry.savedStateHandle.set(saveToKey, defaultGson().toJson(result))
        findNavController().popBackStack(saveToDestId, false)
    }else{
        navController.navigateUp()
    }
}

