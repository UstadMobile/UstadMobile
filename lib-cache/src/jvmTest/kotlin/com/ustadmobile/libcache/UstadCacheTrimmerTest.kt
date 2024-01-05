package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import kotlinx.io.files.SystemFileSystem
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UstadCacheTrimmerTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun givenEntriesExceedSize_whenTrimRun_thenWillEvictRequiredEntries() {
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val md5Digest = Md5Digest()

        val urlPrefix = "http://server.com/file"
        val cacheEntries = (0 until 10).map { index ->
            val url = "$urlPrefix$index"
            CacheEntry(
                key = md5Digest.urlKey(url),
                url = url,
                storageSize = 100_000,
                lastAccessed = index.toLong(),
            )
        }

        cacheDb.cacheEntryDao.insertList(cacheEntries)

        //Add a lock on the first two entries
        (0..1).forEach {
            cacheDb.retentionLockDao.insert(RetentionLock(lockKey = md5Digest.urlKey("$urlPrefix$it")))
        }

        val trimmer = UstadCacheTrimmer(
            db = cacheDb, fileSystem = SystemFileSystem, sizeLimit = { 500_000 }
        )
        trimmer.trim()

        //Evictable size = 800_000 (lock entries don't count towards size)
        //first two entries should remain present  as they were locked
        //last three entries should be present (most recently accessed)
        val expectedIndexes = listOf(0, 1, 5, 6, 7, 8, 9)
        (0 until 10).forEach {
            val entry = cacheDb.cacheEntryDao.findEntryAndBodyByKey(
                md5Digest.urlKey("$urlPrefix$it")
            )

            if(it in expectedIndexes) {
                assertNotNull(entry, "Index $it should have remained in cache")
            }else {
                assertNull(entry, "Index $it should not be in cache")
            }
        }
    }

}