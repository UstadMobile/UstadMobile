package com.ustadmobile.libcache.db

import com.ustadmobile.door.migration.DoorMigrationStatementList
import com.ustadmobile.door.util.systemTimeInMillis

@Suppress("unused")
val MIGRATE_1_2 = DoorMigrationStatementList(1, 2) { db ->
    val lastValidatedTime = systemTimeInMillis()
    buildList {
        add("ALTER TABLE CacheEntry RENAME to CacheEntry_OLD")
        add("CREATE TABLE IF NOT EXISTS CacheEntry (  url  TEXT  NOT NULL , message  TEXT  NOT NULL , statusCode  INTEGER  NOT NULL , cacheFlags  INTEGER  NOT NULL , method  INTEGER  NOT NULL , lastAccessed  INTEGER  NOT NULL , lastValidated  INTEGER  NOT NULL , responseBodySha256  TEXT , responseHeaders  TEXT  NOT NULL , ceId  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
        add("""
            INSERT INTO CacheEntry (url, message, statusCode, cacheFlags, method, lastAccessed, lastValidated, responseBodySha256, responseHeaders, ceId) 
            SELECT url, 'OK', 200, cacheFlags, method, lastAccessed, $lastValidatedTime, responseBodySha256, responseHeaders, ceId 
            FROM CacheEntry_OLD
            """
        )
        add("DROP TABLE CacheEntry_OLD")
    }
}
