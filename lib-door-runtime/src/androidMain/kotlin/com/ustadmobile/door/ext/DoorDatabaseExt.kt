package com.ustadmobile.door.ext

import androidx.room.RoomDatabase
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseVersion
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.asRepository
import java.lang.RuntimeException
import androidx.room.*

private val dbVersions = mutableMapOf<Class<*>, Int>()

actual fun DoorDatabase.dbType(): Int = DoorDbType.SQLITE

actual fun DoorDatabase.dbSchemaVersion(): Int {
    val javaClass = this::class.java
    var thisVersion = dbVersions[javaClass] ?: -1
    if(thisVersion == -1) {
        val clazzName = javaClass.canonicalName!!.substringBefore('_') + "_DoorVersion"
        try {
            thisVersion = (Class.forName(clazzName).newInstance() as DoorDatabaseVersion).dbVersion
            dbVersions[javaClass] = thisVersion
        }catch(e: Exception) {
            throw RuntimeException("Could not determine schema version of ${this::class}")
        }
    }

    return thisVersion
}

actual suspend inline fun <T: DoorDatabase, R> T.doorWithTransaction(crossinline block: suspend(T) -> R): R {
    return withTransaction {
        block(this)
    }
}
