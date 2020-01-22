package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseVersion
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.asRepository
import java.lang.RuntimeException

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