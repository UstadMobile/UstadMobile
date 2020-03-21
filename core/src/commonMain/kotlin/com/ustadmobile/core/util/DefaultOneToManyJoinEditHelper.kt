package com.ustadmobile.core.util

import com.ustadmobile.core.db.dao.OneToManyJoinDao
import kotlinx.atomicfu.atomic
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

class DefaultOneToManyJoinEditHelper<T>(pkGetter: (T) -> Long,
                                        serializationKey: String,
                                        serializationStrategy: SerializationStrategy<List<T>>,
                                        deserializationStrategy: DeserializationStrategy<List<T>>,
                                        pkSetter: T.(Long) -> Unit)
    : OneToManyJoinEditHelper<T, Long>(pkGetter, serializationKey, serializationStrategy,
        deserializationStrategy, 0L, pkSetter, {-1L}) {

    private val atomicLong = atomic(0L)

    override val fakePkGenerator: () -> Long
        get() = { atomicLong.decrementAndGet() }

    override suspend fun commitToDatabase(dao: OneToManyJoinDao<T>, fkSetter: (T) -> Unit) {
        super.commitToDatabase(dao, fkSetter)
        dao.deactivateByUids(entitiesToDeactivate.map { pkGetter(it) })
    }
}