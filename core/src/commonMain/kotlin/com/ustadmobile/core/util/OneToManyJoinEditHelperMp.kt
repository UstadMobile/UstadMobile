package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * OneToManyJoinEditHelperMp (Multiplatform) builds on DefaultOneToManyJoinEditHelper. It can now
 * handle observing the savedState to catch values being returned from other views (e.g. where the user
 * navigates to a picker or edit screen to select the entity that will be joined).
 *
 * For this to work, whenever the user is being directed to edit/pick an entity, ARG_RESULT_DEST_KEY
 * should be set to the returnSavedStateKey val so that it gets picked up and handled as expected.
 */
class OneToManyJoinEditHelperMp<T : Any>(pkGetter: (T) -> Long,
                                         serializationKey: String,
                                         serializationStrategy: SerializationStrategy<List<T>>,
                                         deserializationStrategy: DeserializationStrategy<List<T>>,
                                         editPresenter: UstadEditPresenter<*, *>,
                                         val savedStateHandle: UstadSavedStateHandle,
                                         entityClass: KClass<T>,
                                         val returnSavedStateKey: String = "${serializationKey}_ret",
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
        savedStateHandle.getLiveData<String?>(returnSavedStateKey).observe(editPresenter.lifecycleOwner) {
            if(it == null)
                return@observe

            val newValue = Json.decodeFromString(deserializationStrategy, it).firstOrNull() ?: return@observe
            onEditResult(newValue)
            savedStateHandle[returnSavedStateKey] = null
        }
    }


}