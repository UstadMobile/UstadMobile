package com.ustadmobile.door

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

actual class DoorSqlitePrimaryKeyManager actual constructor(val repo: DoorDatabaseSyncRepository){

    private val keyMap: MutableMap<Int, AtomicLong> = ConcurrentHashMap()

    actual suspend fun getAndIncrementSqlitePk(tableId: Int, increment: Int): Long {
        val key = keyMap.getOrPut(tableId) {
            AtomicLong(repo.syncHelperEntitiesDao.selectNextSqliteSyncablePk(tableId))
        }

        val newKey = key.getAndAdd(increment.toLong())
        repo.syncHelperEntitiesDao.incrementNextSqliteSyncablePk(tableId, increment)
        return newKey
    }

}