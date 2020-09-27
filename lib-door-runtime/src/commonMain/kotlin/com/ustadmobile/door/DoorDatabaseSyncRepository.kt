package com.ustadmobile.door

import kotlin.reflect.KClass

interface DoorDatabaseSyncRepository: DoorDatabaseRepository {

    suspend fun sync(tablesToSync: List<KClass<*>>?)

    suspend fun selectNextSqliteSyncablePk(tableId: Int): Long

    suspend fun incrementNextSqliteSyncablePk(tableId: Int, increment: Int)

    suspend fun getAndIncrementSqlitePk(tableId: Int, increment: Int): Long


    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}