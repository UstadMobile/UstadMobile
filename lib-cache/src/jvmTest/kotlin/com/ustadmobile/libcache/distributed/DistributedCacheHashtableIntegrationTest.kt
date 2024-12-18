package com.ustadmobile.libcache.distributed

import app.cash.turbine.test
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.xxhashkmp.commonjvmimpl.XXStringHasherCommonJvm
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class DistributedCacheHashtableIntegrationTest {

    private val xxStringHasher = XXStringHasherCommonJvm()

    /**
     * 'Hello World' integration test:
     */
    @Test
    fun givenTwoNeighborCaches_whenDiscovered_thenShouldExchangeAvailabilityInfo() {
        Napier.base(DebugAntilog())

        val (cacheDb1, cacheDb2) = (1..2).map {
            DatabaseBuilder.databaseBuilder(
                UstadCacheDb::class, "jdbc:sqlite::memory:", it.toLong())
                .build()
        }.zipWithNext().first()

        val exampleUrls = (1..2).map {
            "https://example.org/file$it.html"
        }

        val md5Digest = Md5Digest()

        //Add entry to cache1
        cacheDb1.cacheEntryDao.insertList(
            listOf(
                CacheEntry(
                    url = exampleUrls.first(),
                    key = md5Digest.urlKey(exampleUrls.first())
                )
            )
        )

        val dCacheTable1 = DistributedCacheHashtable(
            cacheDb = cacheDb1,
            httpPort = 42,
            logger = NapierLoggingAdapter(),
            xxStringHasher = xxStringHasher,
        )

        val dCacheTable2 = DistributedCacheHashtable(
            cacheDb = cacheDb2,
            httpPort = 42,
            logger = NapierLoggingAdapter(),
            xxStringHasher = xxStringHasher,
        )

        runBlocking {
            cacheDb1.neighborCacheDao.upsertAsync(
                NeighborCache(
                    neighborUid = xxStringHasher.neighborUid(
                        "127.0.0.1", dCacheTable2.port
                    ),
                    neighborIp = "127.0.0.1",
                    neighborUdpPort = dCacheTable2.port,
                )
            )

            //Add cache1 as a neighbor on cache2
            cacheDb2.neighborCacheDao.upsertAsync(
                NeighborCache(
                    neighborUid = xxStringHasher.neighborUid(
                        "127.0.0.1", dCacheTable1.port
                    ),
                    neighborIp = "127.0.0.1",
                    neighborUdpPort = dCacheTable1.port,
                )
            )


            val expectedUrlHash = xxStringHasher.hash(exampleUrls.first())
            cacheDb2.neighborCacheEntryDao.allEntriesAsFlow().filter {
                it.any { entry -> entry.nceUrlHash == expectedUrlHash }
            }.test(name = "Await cache2 to receive hashes from cache1", timeout = 10.seconds) {
                val neighborHashesReceived = awaitItem()
                assertEquals(expectedUrlHash, neighborHashesReceived.first().nceUrlHash)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}