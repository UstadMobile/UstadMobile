package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenterJsonLoader
import com.ustadmobile.core.db.dao.OneToManyJoinDao
import kotlinx.atomicfu.atomic
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.kodein.di.DI
import kotlin.reflect.KClass

open class DefaultOneToManyJoinEditHelper<T: Any>(
    pkGetter: (T) -> Long,
    serializationKey: String,
    serializationStrategy: SerializationStrategy<List<T>>,
    deserializationStrategy: DeserializationStrategy<List<T>>,
    editPresenter: UstadEditPresenterJsonLoader,
    di: DI,
    entityClass: KClass<T>,
    pkSetter: T.(Long) -> Unit
) : OneToManyJoinEditHelper<T, Long>(pkGetter, serializationKey, serializationStrategy,
        deserializationStrategy, 0L, editPresenter, di, entityClass, pkSetter, {-1L}){

    private val atomicLong = atomic(0L)

    override fun onLoadFromJsonSavedState(savedState: Map<String, String>?) {
        super.onLoadFromJsonSavedState(savedState)

        savedState?.get("$serializationKey$SUFFIX_PKS_TO_INSERT")?.takeIf { it.isNotEmpty() }?.also {pksToInsertStr ->
            pksToInsert.addAll(pksToInsertStr.split(',').map { it.toLong() })
        }

        savedState?.get("$serializationKey$SUFFIX_PKS_TO_DEACTIVATE")?.takeIf { it.isNotEmpty() }?.also {pksToDeactivateStr ->
            pksToDeactivate.addAll(pksToDeactivateStr.split(',').map { it.toLong() })
        }

        val fakePkStartVal = (liveList.getValue()?.map { pkGetter(it) }?.minOrNull() ?: 0L) -1
        atomicLong.value = fakePkStartVal
    }

    override val fakePkGenerator: () -> Long
        get() = { atomicLong.decrementAndGet() }

    @Suppress("DEPRECATION")
    suspend fun commitToDatabase(
        dao: OneToManyJoinDao<in T>,
        deactivateFn: suspend (keysToDeactivate: List<Long>) -> Unit,
        fkSetter: (T) -> Unit
    ) {
        super.commitToDatabase(dao, fkSetter)
        deactivateFn(primaryKeysToDeactivate)
    }

    override fun doesNewEntityRequireFakePk(pk: Long) = (pk == 0L)

    override fun onSaveState(outState: MutableMap<String, String>) {
        super.onSaveState(outState)

        outState["$serializationKey$SUFFIX_PKS_TO_INSERT"] = pksToInsert.joinToString(separator = ",")
        outState["$serializationKey$SUFFIX_PKS_TO_DEACTIVATE"] = pksToDeactivate.joinToString(separator = ",")
    }



}