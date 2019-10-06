package com.ustadmobile.door

import kotlin.reflect.KClass

interface DoorDatabaseSyncRepository: DoorDatabaseRepository {

    suspend fun sync(tablesToSync: List<KClass<*>>?)


    /**
     * Do not call this on the main thread: it might run a query
     */
    val clientId: Int
}