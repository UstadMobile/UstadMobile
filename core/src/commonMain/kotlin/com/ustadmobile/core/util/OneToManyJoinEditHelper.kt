package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenterJsonLoader
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.dao.OneToManyJoinDao
import com.ustadmobile.door.lifecycle.MutableLiveData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.kodein.di.DI
import kotlin.reflect.KClass

/**
 * This class is designed to help manage a one to many join in edit mode. E.g. Clazz has a 1:n
 * join with Schedule. The editing of that entity is done in memory and passed to/from presenters
 * using JSON.
 *
 * At the end of editing mode newly created entities must be joined and inserted, changed entities
 * must be updated and some entities will need to be deactivated (e.g. the active field is set to
 * false).
 */
open class OneToManyJoinEditHelper<T : Any, K>(val pkGetter: (T) -> K,
                                               val serializationKey: String,
                                               val serializationStrategy: SerializationStrategy<List<T>>? = null,
                                               val deserializationStrategy: DeserializationStrategy<List<T>>? = null,
                                               val newPk: K,
                                               editPresenter: UstadEditPresenterJsonLoader,
                                               val di: DI,
                                               val entityClass: KClass<T>,
                                               val pkSetter: T.(K) -> Unit,
                                               open protected val fakePkGenerator: () -> K): UstadEditPresenter.JsonLoadListener  {

    val liveList: MutableLiveData<List<T>> = MutableLiveData(listOf())

    protected val pksToInsert = mutableListOf<K>()

    protected val pksToDeactivate = mutableListOf<K>()

    init {
        editPresenter.addJsonLoadListener(this)
    }

    fun onEditResult(entity: T) {
        val pk = pkGetter(entity)
        val currentList = liveList.getValue() ?: return
        val entityIndex = currentList.indexOfFirst { pkGetter(it) == pk }
        if(entityIndex == -1){
            if(doesNewEntityRequireFakePk(pkGetter(entity))) {
                pkSetter(entity, fakePkGenerator())
            }

            pksToInsert += pkGetter(entity)
            val listVal = liveList.getValue() ?: return
            val newList = listVal + entity
            liveList.setValue(newList)
        }else {
            val newList = currentList.toMutableList()
            newList[entityIndex] = entity
            liveList.setValue(newList)
        }
    }

    fun onDeactivateEntity(entity: T) {
        val listVal = liveList.getValue()?.toMutableList() ?: return
        val pkToRemove = pkGetter(entity)
        liveList.postValue(listVal.filter { pkGetter(it) != pkToRemove} )
        pksToDeactivate += pkToRemove
    }

    val entitiesToInsert: List<T>
        get() {
            val listVal = liveList.getValue() ?: return listOf()
            return listVal.filter {pkGetter(it) in pksToInsert}
        }

    //TODO: Track which entities have been edited and return only those (e.g. don't update those that weren't actually changed)
    val entitiesToUpdate: List<T>
        get() = liveList.getValue()?.filter { pkGetter(it) !in pksToInsert } ?: listOf()

    val primaryKeysToDeactivate: List<K>
        get() = pksToDeactivate.toList()

    override fun onSaveState(outState: MutableMap<String, String>) {
        val listVal = liveList.getValue() ?: return
        val serializer = serializationStrategy ?: return
        outState[serializationKey] = safeStringify(di, serializationStrategy, listVal)
    }

    override fun onLoadFromJsonSavedState(savedState: Map<String, String>?) {
        val listJsonStr = savedState?.get(serializationKey) ?: return
        val deserializer = deserializationStrategy ?: return
        val listVal = safeParseList(di, deserializer, entityClass, listJsonStr)//Json.parse(deserializer, listJsonStr)
        liveList.setValue(listVal)
    }

    /**
     * Commits the results of the editing to the database.
     *
     * Should not be used anymore directly. It cannot take care of deactivation.
     */
    protected open suspend fun commitToDatabase(dao: OneToManyJoinDao<in T>, fkSetter: (T) -> Unit) {
        dao.insertListAsync(entitiesToInsert.also { it.forEach {
            fkSetter(it)
            pkSetter(it, newPk)
        }  })
        dao.updateListAsync(entitiesToUpdate.also { it.forEach(fkSetter) })
    }

    open protected fun doesNewEntityRequireFakePk(pk: K): Boolean {
        return true
    }

    companion object {

        const val SUFFIX_PKS_TO_INSERT = "_pksToInsert"

        const val SUFFIX_PKS_TO_DEACTIVATE = "_pksToDeactivate"

    }

}