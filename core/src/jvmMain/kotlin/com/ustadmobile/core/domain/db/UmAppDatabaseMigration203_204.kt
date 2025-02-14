package com.ustadmobile.core.domain.db

import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigrationAsync

fun UmAppDatabaseMigration203_204(xxHasher64Factory: XXHasher64Factory) = DoorMigrationAsync(
    203, 204
){ db ->
    db.connection.migrate203_204AddStatementEntityContextRegHash(xxHasher64Factory, db.dbType())
}
