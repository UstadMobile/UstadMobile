package com.ustadmobile.libcache.db

import com.ustadmobile.door.migration.DoorMigrationStatementList
import com.ustadmobile.door.util.systemTimeInMillis

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

/**
 * Converts from using the url as the lookup to using a key (MD5 of the URL, base64 encoded)
 */
val MIGRATE_2_3 = DoorMigrationStatementList(2,3) {
    buildList {
        add("DROP TABLE CacheEntry")
        add("DROP TABLE RequestedEntry")
        add("CREATE TABLE IF NOT EXISTS CacheEntry (  key  TEXT  PRIMARY KEY  NOT NULL , url  TEXT  NOT NULL , message  TEXT  NOT NULL , statusCode  INTEGER  NOT NULL , cacheFlags  INTEGER  NOT NULL , method  INTEGER  NOT NULL , lastAccessed  INTEGER  NOT NULL , lastValidated  INTEGER  NOT NULL , responseBodySha256  TEXT , responseHeaders  TEXT  NOT NULL )")
        add("CREATE TABLE IF NOT EXISTS RequestedEntry (  requestSha256  TEXT  NOT NULL , requestedKey  TEXT  NOT NULL , batchId  INTEGER  NOT NULL , id  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
    }
}

/**
 * Add RetentionLock table
 * Update body table to add size field
 */
val MIGRATE_3_4 = DoorMigrationStatementList(3, 4) {
    buildList {
        add("CREATE TABLE IF NOT EXISTS RetentionLock (  lockKey  TEXT  NOT NULL , lockRemark  TEXT  NOT NULL , lockId  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
        add("ALTER TABLE RequestBody ADD COLUMN bodySize  INTEGER  NOT NULL  DEFAULT 0")
    }
}


/**
 * Drop the one-many cache entry to request body concept. It isn't needed because where duplicate
 * urls are likely the urls themselves are a function of the sha-256 sum.
 *
 * CacheEntry now has the storageUri and size on it directly. This is still not deployed on production,
 * so migration is mostly destructive.
 */
val MIGRATE_4_5 = DoorMigrationStatementList(4, 5) {
    buildList {
        add("DROP TABLE IF EXISTS RequestBody")
        add("DROP TABLE IF EXISTS CacheEntry")
        add("CREATE TABLE IF NOT EXISTS CacheEntry (  key  TEXT  PRIMARY KEY  NOT NULL , url  TEXT  NOT NULL , message  TEXT  NOT NULL , statusCode  INTEGER  NOT NULL , cacheFlags  INTEGER  NOT NULL , method  INTEGER  NOT NULL , lastAccessed  INTEGER  NOT NULL , lastValidated  INTEGER  NOT NULL , responseBodySha256  TEXT , responseHeaders  TEXT  NOT NULL , storageUri  TEXT  NOT NULL , storageSize  INTEGER  NOT NULL )")
    }
}

/**
 * Add index for last accessed column (sort criteria) and lock key
 */
val MIGRATE_5_6 = DoorMigrationStatementList(5, 6) {
    buildList {
        add("CREATE INDEX idx_lastAccessed ON CacheEntry (lastAccessed)")
        add("CREATE INDEX idx_lockKey ON RetentionLock (lockKey)")
    }
}

/**
 * Rename responsebody field to integrity field on CacheEntry
 */
val MIGRATE_6_7 = DoorMigrationStatementList(6, 7) {
    buildList {
        add("DROP TABLE IF EXISTS CacheEntry")
        add("CREATE TABLE IF NOT EXISTS CacheEntry (  key  TEXT  PRIMARY KEY  NOT NULL , url  TEXT  NOT NULL , message  TEXT  NOT NULL , statusCode  INTEGER  NOT NULL , cacheFlags  INTEGER  NOT NULL , method  INTEGER  NOT NULL , lastAccessed  INTEGER  NOT NULL , lastValidated  INTEGER  NOT NULL , integrity  TEXT , responseHeaders  TEXT  NOT NULL , storageUri  TEXT  NOT NULL , storageSize  INTEGER  NOT NULL )")
        add("CREATE INDEX idx_lastAccessed ON CacheEntry (lastAccessed)")
    }
}
