package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import org.kodein.di.DI

/**
 * @param di KodeIN Dependency Injection
 * @param savedStateHandle SavedStateHandle for the destination. This will have the value of
 * arguments passed when navigating
 */
abstract class UstadEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle){

    /**
     * Finish with result for an edit screen.
     *
     * If the user has just created a new entity and there is no current return result expected
     * by another view, then take the user to the detail screen for the
     * entity just created. The current (edit) view will be popped off the stack, so if the user
     * goes back, they will not go back to the edit screen.
     *
     * @param detailViewName the detail view name to navigate to
     * @param entityUid the entity uid of the entity just saved
     * @param result the actual entity just created
     */
    fun finishWithResult(
        detailViewName: String,
        entityUid: Long,
        result: Any?
    ) {
        val popUpToViewName = savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME]
        val saveToKey = savedStateHandle[UstadView.ARG_RESULT_DEST_KEY]

        val createdNewEntity = savedStateHandle[ARG_ENTITY_UID] != null
        val returnResultExpected = (popUpToViewName != null && saveToKey != null)

        if(!createdNewEntity || returnResultExpected) {
            finishWithResult(result)
        }else {
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