package com.ustadmobile.core.db

import com.ustadmobile.door.migration.DoorMigrationStatementList

val MIGRATE_USERNAME_CLIENT = DoorMigrationStatementList(202, 203) { _ ->
    emptyList()
}