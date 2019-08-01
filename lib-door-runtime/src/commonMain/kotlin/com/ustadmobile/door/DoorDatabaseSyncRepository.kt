package com.ustadmobile.door

import kotlin.reflect.KClass

interface DoorDatabaseSyncRepository {

    fun sync(tablesToSync: List<KClass<*>>)
}