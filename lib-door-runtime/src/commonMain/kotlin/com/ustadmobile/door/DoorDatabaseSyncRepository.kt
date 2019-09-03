package com.ustadmobile.door

import kotlin.reflect.KClass

interface DoorDatabaseSyncRepository {

    suspend fun sync(tablesToSync: List<KClass<*>>?)
}