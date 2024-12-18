package com.ustadmobile.libcache.distributed

import app.cash.turbine.test
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.AddNewEntryTriggerCallback
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class DistributedCacheHashtableIntegrationTest {

    private val xxStringHasher = XXStringHasherCommonJvm()

    private val md5Digest = Md5Digest()

    private val exampleUrls = (1..2).map {
        "https://example.org/file$it.html"
    }

    private val exampleCacheEntries = exampleUrls.map {
        CacheEntry(
            url = it,
            key = md5Digest.urlKey(it)
        )
    }

    inner class DistributedCacheHashtableTestContext(
        val cacheDb1: UstadCacheDb,
        val cacheDb2: UstadCacheDb,
        val dCacheTable1: DistributedCacheHashtable,
        val dCacheTable2: DistributedCacheHashtable,
    ) {
        fun discover() {
            cacheDb1.neighborCacheDao.upsert(
                NeighborCache(
                    neighborUid = xxStringHasher.neighborUid(
                        "127.0.0.1", dCacheTable2.port
                    ),
                    neighborIp = "127.0.0.1",
                    neighborUdpPort = dCacheTable2.port,
                )
            )

            //Add cache1 as a neighbor on cache2
            cacheDb2.neighborCacheDao.upsert(
                NeighborCache(
                    neighborUid = xxStringHasher.neighborUid(
                        "127.0.0.1", dCacheTable1.port
                    ),
                    neighborIp = "127.0.0.1",
                    neighborUdpPort = dCacheTable1.port,
                )
            )
        }
    }

    private fun testDistributedCacheWithTwoNeighbors(
        block: DistributedCacheHashtableTestContext.() -> Unit
    ) {
        val (cacheDb1, cacheDb2) = (1..2).map {
            DatabaseBuilder.databaseBuilder(
                UstadCacheDb::class, "jdbc:sqlite::memory:", it.toLong())
                .addCallback(AddNewEntryTriggerCallback())
                .build()
        }.zipWithNext().first()

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

        val context = DistributedCacheHashtableTestContext(
            cacheDb1 = cacheDb1,
            cacheDb2 = cacheDb2,
            dCacheTable1 = dCacheTable1,
            dCacheTable2 = dCacheTable2,
        )

        try {
            block(context)
        }finally {
            context.dCacheTable1.close()
            context.dCacheTable2.close()
            context.cacheDb1.close()
            context.cacheDb2.close()
        }
    }


    /**
     * 'Hello World' integration test:
     */
    @Test
    fun givenTwoNeighborCaches_whenDiscovered_thenShouldExchangeAvailabilityInfo() {
        Napier.base(DebugAntilog())

        testDistributedCacheWithTwoNeighbors {
            //Add entry to cache1
            cacheDb1.cacheEntryDao.insertList(listOf(exampleCacheEntries.first()))

            discover()

            runBlocking {
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

    @Test
    fun givenTwoNeighborCachesDiscovered_whenNewEntryAdded_thenOtherNodeWillAddToDistributedHash() {
        Napier.base(DebugAntilog())

        testDistributedCacheWithTwoNeighbors {
            //Add entry to cache1
            cacheDb1.cacheEntryDao.insertList(listOf(exampleCacheEntries.first()))

            discover()

            runBlocking {
                val exampleCacheEntry1Hash = xxStringHasher.hash(exampleUrls.first())

                //wait for first entry to be sent
                cacheDb2.neighborCacheEntryDao.allEntriesAsFlow().filter {
                    it.any { entry -> entry.nceUrlHash == exampleCacheEntry1Hash }
                }.first()

                cacheDb1.cacheEntryDao.insertList(listOf(exampleCacheEntries.last()))

                val exampleCacheEntry2Hash = xxStringHasher.hash(exampleUrls.last())
                cacheDb2.neighborCacheEntryDao.allEntriesAsFlow().filter {
                    it.any { entry -> entry.nceUrlHash == exampleCacheEntry2Hash }
                }.first()
            }
        }
    }


}