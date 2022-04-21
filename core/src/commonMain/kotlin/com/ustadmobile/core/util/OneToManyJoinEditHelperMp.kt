package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

/**
 * OneToManyJoinEditHelperMp (Multiplatform) builds on DefaultOneToManyJoinEditHelper. It can now
 * handle observing the savedState to catch values being returned from other views (e.g. where the user
 * navigates to a picker or edit screen to select the entity that will be joined).
 *
 * For this to work, whenever the user is being directed to edit/pick an entity, ARG_RESULT_DEST_KEY
 * should be set to the returnSavedStateKey val so that it gets picked up and handled as expected.
 */
open class OneToManyJoinEditHelperMp<T : Any>(pkGetter: (T) -> Long,
                                         serializationKey: String,
                                         serializationStrategy: SerializationStrategy<List<T>>,
                                         deserializationStrategy: DeserializationStrategy<List<T>>,
                                         private val editPresenter: UstadEditPresenter<*, *>,
                                         val savedStateHandle: UstadSavedStateHandle,
                                         entityClass: KClass<T>,
                                         val returnSavedStateKey: String = serializationKey + SUFFIX_RETKEY_DEFAULT,
                                         pkSetter: T.(Long) -> Unit):
    DefaultOneToManyJoinEditHelper<T>(
        pkGetter,
        serializationKey,
        serializationStrategy,
        deserializationStrategy,
        editPresenter,
        entityClass,
        pkSetter) {


    init {
        editPresenter.observeSavedStateResult(returnSavedStateKey, deserializationStrategy, entityClass) {
            val newValue = it.firstOrNull() ?: return@observeSavedStateResult
            onEditResult(newValue)
            savedStateHandle[returnSavedStateKey] = null
        }
    }


    /**
     * Create NavigateForResultOptions that are linked to this OneToManyJoinEditHelper. This really
     * just a shorthand that also makes sure that the destination result key will match the one that
     * this join edit helper is observing.
     */
    fun createNavigateForResultOptions(destinationViewName: String,
                         serializationStrategy: SerializationStrategy<T>,
                         arguments: MutableMap<String, String> = mutableMapOf()) : NavigateForResultOptions<T>{

        return NavigateForResultOptions(
            currentEntityValue = null,
            fromPresenter = editPresenter,
            entityClass = entityClass,
            destinationViewName = destinationViewName,
            serializationStrategy = serializationStrategy,
            destinationResultKey = returnSavedStateKey,
            overwriteDestination = true,
            arguments = arguments)
    }

    /**
     * Create a NavigateForResultOneToManyJoinEditListener linked to this one-many relationship. It will
     * set the destination result key, and it will automatically call this one-many joinedithelper
     * to delete / deactivate the joined entity.
     *
     * This is appropriate to use for simple cases when the entity type return is exactly the same
     * as used by the JoinEditListener (e.g. what is displayed to the user).
     */
    fun createNavigateForResultListener(editViewName: String,
                                        serializationStrategy: SerializationStrategy<T>,
                                        arguments: MutableMap<String, String> = mutableMapOf()) : NavigateForResultOneToManyJoinEditListener<T>{
        return NavigateForResultOneToManyJoinEditListener(
            createNavigateForResultOptions(destinationViewName = editViewName,
                serializationStrategy = serializationStrategy,
                arguments = arguments), this)
    }

    companion object {

        const val SUFFIX_RETKEY_DEFAULT = "_ret"

    }

}