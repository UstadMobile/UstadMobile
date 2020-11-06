package com.ustadmobile.door

interface SyncableDoorDatabaseWrapper<T : SyncableDoorDatabase> {

    val realDatabase: T

}