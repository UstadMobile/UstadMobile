package com.ustadmobile.core.util

import com.ustadmobile.door.DoorMutableLiveData

/**
 * This class is designed to help manager a one to many join in edit mode. E.g. Clazz has a 1:n
 * join with Schedule. The editing of that entity is done in memory and passed to/from presenters
 * using JSON.
 *
 * At the end of editing mode newly created entities must be joined and inserted, changed entities
 * must be updated and some entities will need to be deactivated (e.g. the active field is set to
 * false).
 */
open class OneToManyJoinEditHelper<T, K>(val pkGetter: (T) -> K,
                                    val pkSetter: T.(K) -> Unit,
    open protected val fakePkGenerator: () -> K) {

    val liveList: DoorMutableLiveData<List<T>> = DoorMutableLiveData(listOf())

    private val pksToInsert = mutableListOf<K>()

    private val pksToDeactivate = mutableListOf<K>()

    fun onEditResult(entity: T) {
        val pk = pkGetter(entity)
        if(pk as? Long == 0L || pk as? Int == 0){
            val newFakePk = fakePkGenerator()
            pkSetter(entity, newFakePk)
            pksToInsert += newFakePk

            val listVal = liveList.getValue() ?: return
            val newList = listVal + entity
            liveList.sendValue(newList)
        }
    }

    fun onDeactivateEntity(entity: T) {
        val listVal = liveList.getValue()?.toMutableList() ?: return
        val pkToRemove = pkGetter(entity)
        liveList.sendValue(listVal.filter { pkGetter(it) != pkToRemove} )
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

    val entitiesToDeactivate: List<T>
        get() = liveList.getValue()?.filter { pkGetter(it) in pksToDeactivate } ?: listOf()
}