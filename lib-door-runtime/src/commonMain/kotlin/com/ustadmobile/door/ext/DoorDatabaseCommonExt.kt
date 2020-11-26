package com.ustadmobile.door.ext

import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.SyncableDoorDatabase

/**
 * Extension property that will be true if this database is both syncable and the primary (eg. server)
 * instance, false otherwise
 */
val DoorDatabase.syncableAndPrimary: Boolean
        get() = (this as? SyncableDoorDatabase)?.master ?: false