package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.dao.OneToManyJoinDao
import kotlinx.atomicfu.atomic
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

class DefaultOneToManyJoinEditHelper<T>(pkGetter: (T) -> Long,
                                        serializationKey: String,
                                        serializationStrategy: SerializationStrategy<List<T>>,
                                        deserializationStrategy: DeserializationStrategy<List<T>>,
                                        editPresenter: UstadEditPresenter<*, *>,
                                        pkSetter: T.(Long) -> Unit)
    : OneToManyJoinEditHelper<T, Long>(pkGetter, serializationKey, serializationStrategy,
        deserializationStrategy, 0L, editPresenter, pkSetter, {-1L}){

    private val atomicLong = atomic(0L)

    override fun onLoadFromJsonSavedState(savedState: Map<String, String>?) {
        super.onLoadFromJsonSavedState(savedState)

        val fakePkStartVal = (liveList.getValue()?.map { pkGetter(it) }?.min() ?: 0L) -1
        atomicLong.value = fakePkStartVal
    }

    override val fakePkGenerator: () -> Long
        get() = { atomicLong.decrementAndGet() }

    override suspend fun commitToDatabase(dao: OneToManyJoinDao<T>, fkSetter: (T) -> Unit) {
        super.commitToDatabase(dao, fkSetter)
        dao.deactivateByUids(primaryKeysToDeactivate)
    }

    override open fun doesNewEntityRequireFakePk(pk: Long) = (pk == 0L)

}