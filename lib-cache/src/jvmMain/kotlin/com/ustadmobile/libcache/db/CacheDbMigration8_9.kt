package com.ustadmobile.libcache.db

import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.migration.DoorMigrationStatementList
import java.io.File
import java.sql.Connection


/*
 * Previous bug moved CacheEntry to persistent folder when lock was added, but did not update
 * database (update was in LRU only)
 */
val MIGRATE_8_9 = DoorMigrationStatementList(8, 9) { db: DoorSqlDatabase ->
    buildList {
        db.connection.also { connection: Connection ->
            connection.createStatement().use { stmt ->
                stmt.executeQuery("""
                    SELECT CacheEntry.key AS key, 
                           CacheEntry.storageUri AS storageUri
                      FROM CacheEntry
                """).use { results ->
                    while(results.next()) {
                        val storageUri = results.getString("storageUri")
                        val storageFile = File(storageUri)
                        val sepChar = File.separatorChar
                        val persistentPathStr = storageUri
                            .replace("${sepChar}cache${sepChar}", "${sepChar}persistent${sepChar}")
                        val persistentFile = File(persistentPathStr)
                        val key = results.getString("key")
                        if(!storageFile.exists() && persistentFile.exists()) {
                            println("Repairing: $storageUri was moved to $persistentPathStr")
                            add("UPDATE CacheEntry SET storageUri = '$persistentPathStr' WHERE key = '$key'")
                        }
                    }
                }
            }
        }
    }
}
