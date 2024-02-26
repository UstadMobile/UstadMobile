package com.ustadmobile.libcache.db

import com.ustadmobile.door.migration.DoorMigrationStatementList

/**
 * Issue did not escape on Android
 */
val MIGRATE_8_9 = DoorMigrationStatementList(8, 9) {
    emptyList()
}

