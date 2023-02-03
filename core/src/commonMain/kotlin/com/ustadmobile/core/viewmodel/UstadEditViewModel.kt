package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import org.kodein.di.DI

abstract class UstadEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle){

    fun onEditFinish(
        detailViewName: String,
        entityUid: Long,
        result: Any?
    ) {
        val popUpToViewName = savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME]
        val saveToKey = savedStateHandle[UstadView.ARG_RESULT_DEST_KEY]

        if(savedStateHandle[ARG_ENTITY_UID] != null || (popUpToViewName != null && saveToKey != null)) {
            //use the normal finish with result - e.g. just go back
            finishWithResult(result)
        }else {
            //a new entity was just created, go to the detail view (and remove the current view
            //from the back stack
            navController.navigate(
                viewName = detailViewName,
                args = mapOf(ARG_ENTITY_UID to entityUid.toString()),
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = CURRENT_DEST,
                    popUpToInclusive = true
                )
            )
        }
    }
}