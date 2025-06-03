package com.ustadmobile.core.domain.db

import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigrationAsync
import com.ustadmobile.xxhashkmp.XXHasher64Factory

fun UmAppDatabaseMigration203_204(xxHasher64Factory: XXHasher64Factory) = DoorMigrationAsync(
    203, 204
){ db ->
    db.connection.migrate203_204AddStatementEntityContextRegHash(xxHasher64Factory, db.dbType())
}
