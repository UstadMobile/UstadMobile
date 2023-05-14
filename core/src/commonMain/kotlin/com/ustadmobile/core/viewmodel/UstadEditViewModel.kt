package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import org.kodein.di.DI

/**
 * @param di KodeIN Dependency Injection
 * @param savedStateHandle SavedStateHandle for the destination. This will have the value of
 * arguments passed when navigating
 */
abstract class UstadEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String,
) : UstadViewModel(di, savedStateHandle, destinationName){

    protected var saveStateJob: Job? = null

    protected val entityUidArg: Long by lazy(LazyThreadSafetyMode.NONE) {
        savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0
    }

    /**
     * Schedule saving the entity to the savedstate handle. This will cancel previous jobs (e.g.
     * to avoid triggering a save for every keystroke)
     */
    protected inline fun <reified T> scheduleEntityCommitToSavedState(
        entity: T?,
        key: String = KEY_ENTITY_STATE,
        serializer: SerializationStrategy<T>,
        commitDelay: Long = COMMIT_DELAY,
    ) {
        saveStateJob?.cancel()
        saveStateJob = viewModelScope.launch {
            delay(commitDelay)
            if(entity != null) {
                savedStateHandle.setJson(key, serializer, entity)
            }
        }
    }

    /**
     * Finish with result for an edit screen.
     *
     * If the user has just created a new entity and there is no current return result expected
     * by another view, then take the user to the detail screen for the
     * entity just created. The current (edit) view will be popped off the stack, so if the user
     * goes back, they will not go back to the edit screen.
     *
     * If the user is editing an existing entity, or there is currently a return result expected,
     * then we will "return" the result using finishWithResult
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

        val createdNewEntity = savedStateHandle[ARG_ENTITY_UID] == null
        val returnResultExpected = (popUpToViewName != null && saveToKey != null)

        if(createdNewEntity && !returnResultExpected) {
            navController.navigate(
                viewName = detailViewName,
                args = mapOf(ARG_ENTITY_UID to entityUid.toString()),
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = CURRENT_DEST,
                    popUpToInclusive = true
                )
            )
        }else {
            finishWithResult(result)
        }
    }

    /**
     * Simple function to get the title for an edit view where there is one message id for editing
     * an existing entity and another message id for the title if creating a new entity.
     */
    protected fun createEditTitle(
        newEntityMessageId: Int,
        editEntityMessageId: Int,
    ): String {
        val isEditing = entityUidArg != 0L || ARG_ENTITY_JSON in savedStateHandle.keys
        return systemImpl.getString(
            if(isEditing) {
                editEntityMessageId
            }else {
                newEntityMessageId
            }
        )
    }

    /**
     * Shorthand to check if an error message state should be cleared. If there is no error message,
     * return null. If the new value has changed, clear the error message. Otherwise leave the
     * error message as is
     */
    protected fun updateErrorMessageOnChange(
        prevFieldValue: Any?,
        currentFieldValue: Any?,
        currentErrorMessage: String?,
    ): String? {
        return if(currentErrorMessage == null)
            null
        else if(prevFieldValue != currentFieldValue)
            null
        else
            currentErrorMessage
    }

    companion object {

        /**
         * The default delay between the user making a change and committing the entity value to
         * savedstate.
         */
        const val COMMIT_DELAY = 200L
    }
}