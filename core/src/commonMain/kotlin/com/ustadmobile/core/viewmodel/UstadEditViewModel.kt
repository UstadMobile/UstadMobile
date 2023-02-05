package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.cancelPrevJobAndLaunchDelayed
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import kotlinx.coroutines.Job
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

    protected var saveStateJob: Job? = null

    /**
     * Load an entity for editing:
     *
     * 1. Try to load from JSON in saved state. This normally looks at KEY_ENTITY_STATE first,
     *    then ARG_ENTITY_JSON for a value that has been passed as an argument.
     * 2. If nothing found in loadFromStateKeys, then try to load from the database and then the
     *    repository. If loading from the repository fails, use the value from the database.
     * 3. If nothing is found in the database/repository, then make a default value
     *
     * @param loadFromStateKeys a list of keys to look for a saved value (in order to check).
     * @param savedStateKey the key that should be used to save the current value (e.g. as the user
     *        edits).
     * @param onLoadFromDb a function that will load the given entity from the database and/or repo
     * @param makeDefault a function that will create a default value if nothing is found in the
     *        database or repo.
     * @param uiUpdate the functin that will update the UI to display the given entity. When loading
     *        from the database/repository, the value that is loaded from the local database will
     *        be displayed first (so the user sees this whilst the value is loading from the repo).
     * @param T the entity type
     */
    protected inline fun <reified T> loadEntity(
        loadFromStateKeys: List<String> = listOf(KEY_ENTITY_STATE, UstadEditView.ARG_ENTITY_JSON),
        savedStateKey: String = loadFromStateKeys.first(),
        onLoadFromDb: (UmAppDatabase) -> T?,
        makeDefault: () -> T,
        uiUpdate: (T) -> Unit,
    ) : T {

        loadFromStateKeys.forEach { key ->
            val savedVal: T? = savedStateHandle.getJson<T>(key)
            if(savedVal != null) {
                uiUpdate(savedVal)
                return savedVal
            }
        }

        val dbVal = onLoadFromDb(activeDb)
        if(dbVal != null) {
            uiUpdate(dbVal)
        }

        try {
            val repoVal = onLoadFromDb(activeRepo) ?: makeDefault()
            savedStateHandle.setJson(savedStateKey, repoVal)
            uiUpdate(repoVal)
            return repoVal
        }catch(e: Exception) {
            //could happen when connectivity is not so good

            savedStateHandle.setJson(savedStateKey, dbVal)
            return dbVal ?: makeDefault().also(uiUpdate)
        }
    }

    /**
     * Commit an entity to the savedStateHandle if the entity is not null.
     *
     * @param entity the entity to save
     * @param key the key to use for the savedStateHandle
     */
    protected inline fun <reified T> commitEntityToSavedState(
        entity: T?,
        key: String = KEY_ENTITY_STATE,
    ) {
        if(entity != null) {
            savedStateHandle.setJson(key, entity)
        }
    }

    /**
     * Schedule saving the entity to the savedstate handle. This will cancel previous jobs (e.g.
     * to avoid triggering a save for every keystroke)
     */
    protected inline fun <reified T> scheduleEntityCommitToSavedState(
        entity: T?,
        key: String = KEY_ENTITY_STATE
    ) {
        saveStateJob = viewModelScope.cancelPrevJobAndLaunchDelayed(saveStateJob, COMMIT_DELAY) {
            commitEntityToSavedState(entity = entity, key = key)
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

    companion object {

        /**
         * The default delay between the user making a change and committing the entity value to
         * savedstate.
         */
        const val COMMIT_DELAY = 100L
    }
}