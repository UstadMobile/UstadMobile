package com.ustadmobile.port.android.view

import androidx.navigation.fragment.findNavController
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.view.UstadEditView

abstract class UstadEditFragment<T>: UstadBaseFragment(), UstadEditView<T> {

    override fun finishWithResult(result: List<T>) {
        val saveToDestination = arguments?.getString(ARG_RESULT_DEST_ID)
        val saveToKey = arguments?.getString(ARG_RESULT_DEST_KEY)
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

}