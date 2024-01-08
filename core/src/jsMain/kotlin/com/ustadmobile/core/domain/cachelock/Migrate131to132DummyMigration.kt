package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.migration.DoorMigrationStatementList

/**
 * On the server, migrating 131 to 132 means adding triggers to create cache locks for uris to retain
 * On other platforms, it is a dummy migration
 */
val Migrate131to132DummyMigration = DoorMigrationStatementList(131, 132) {
    emptyList()
}
