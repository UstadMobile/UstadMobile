package com.ustadmobile.door.ext

import androidx.room.RoomDatabase
import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorSyncCallback

/**
 *
 */
fun RoomDatabase.initSyncablePrimaryKeys() {
    try {
        val syncCallback= Class.forName("${this::class.java.superclass?.canonicalName}_SyncCallback") as DoorSyncCallback
        openHelper.writableDatabase.use {
            syncCallback.initSyncablePrimaryKeys(it)
        }
    }catch(e: Exception) {
        Napier.e("Exception initializing syncable entities", e)
    }
}