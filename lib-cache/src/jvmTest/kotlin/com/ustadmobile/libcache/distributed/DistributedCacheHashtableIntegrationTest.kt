package com.ustadmobile.libcache.distributed

import app.cash.turbine.test
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheImpl
import com.ustadmobile.libcache.db.AddNewEntryTriggerCallback
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.util.storeFileAsUrl
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.xxhashkmp.commonjvmimpl.XXStringHasherCommonJvm
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DistributedCacheHashtableIntegrationTest {

    private val xxStringHasher = XXStringHasherCommonJvm()

    @get:Rule
    val tempDir = TemporaryFolder()

    private val exampleUrls = (1..2).map {
        "https://example.org/file$it.html"
    }

    private inner class TestCachePathProvider(val rootPath: Path): CachePathsProvider {
        override fun invoke(): CachePaths {
            return CachePaths(
                tmpWorkPath = Path(rootPath, "tmpWork"),
                persistentPath = Path(rootPath, "persistent"),
                cachePath = Path(rootPath, "cache")
            )
        }
    }

    inner class DistributedCacheHashtableTestContext(
        val cacheDb1: UstadCacheDb,
        val cacheDb2: UstadCacheDb,
        val cache1: UstadCache,
        val cache2: UstadCache,
        val dCacheTable1: DistributedCacheHashtable,
        val dCacheTable2: DistributedCacheHashtable,
    ) {
        fun discover() {
            //Add cache2 as a neighbor on cache1
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

        val rootDir1 = Path(tempDir.newFolder("cache1").absolutePath)
        val rootDir2 = Path(tempDir.newFolder("cache2").absolutePath)


        val cache1 = UstadCacheImpl(
            pathsProvider = TestCachePathProvider(rootDir1),
            db = cacheDb1,
            xxStringHasher = XXStringHasherCommonJvm(),
            databaseCommitInterval = 100,
        )

        val cache2 = UstadCacheImpl(
            pathsProvider = TestCachePathProvider(rootDir2),
            db = cacheDb2,
            xxStringHasher = XXStringHasherCommonJvm(),
            databaseCommitInterval = 100,
        )

        val dCacheTable1 = DistributedCacheHashtable(
            cacheDb = cacheDb1,
            httpPort = 42,
            logger = NapierLoggingAdapter(),
            xxStringHasher = xxStringHasher,
            deviceName = { "cache1" },
            pingInterval = PING_INTERVAL,
        )

        val dCacheTable2 = DistributedCacheHashtable(
            cacheDb = cacheDb2,
            httpPort = 42,
            logger = NapierLoggingAdapter(),
            xxStringHasher = xxStringHasher,
            deviceName = { "cache2" },
            pingInterval = PING_INTERVAL,
        )

        val context = DistributedCacheHashtableTestContext(
            cacheDb1 = cacheDb1,
            cacheDb2 = cacheDb2,
            cache1 = cache1,
            cache2 = cache2,
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
            context.cache1.close()
            context.cache2.close()
        }
    }


    /**
     * 'Hello World' integration test:
     */
    @Test
    fun givenTwoNeighborCaches_whenDiscovered_thenShouldExchangeAvailabilityInfo() {
        testDistributedCacheWithTwoNeighbors {
            //Add entry to cache1
            cache1.storeFileAsUrl(
                testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
                testUrl = exampleUrls.first(),
                mimeType = "image/png"
            )

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
        testDistributedCacheWithTwoNeighbors {
            cache1.storeFileAsUrl(
                testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
                testUrl = exampleUrls.first(),
                mimeType = "image/png"
            )

            discover()

            runBlocking {
                val exampleCacheEntry1Hash = xxStringHasher.hash(exampleUrls.first())

                //wait for first entry to be sent
                cacheDb2.neighborCacheEntryDao.allEntriesAsFlow().filter {
                    it.any { entry -> entry.nceUrlHash == exampleCacheEntry1Hash }
                }.first()

                cache1.storeFileAsUrl(
                    testFile = tempDir.newFileFromResource(this::class.java, "/testfile2.png"),
                    testUrl = exampleUrls.last(),
                    mimeType = "image/png"
                )

                val exampleCacheEntry2Hash = xxStringHasher.hash(exampleUrls.last())
                cacheDb2.neighborCacheEntryDao.allEntriesAsFlow().filter {
                    it.any { entry -> entry.nceUrlHash == exampleCacheEntry2Hash }
                }.first()
            }
        }
    }

    @Test
    fun givenTwoNeighborCachesDiscovered_thenPingTimesWillBeDetermined() {
        testDistributedCacheWithTwoNeighbors {
            discover()

            runBlocking {
                listOf(cacheDb1, cacheDb2).forEach { cacheDb ->
                    cacheDb.neighborCacheDao.allNeighborsAsFlow().filter { list ->
                        list.isNotEmpty() && list.all { it.neighborPingTime > 0 }
                    }.test(
                        timeout = 5_000.milliseconds, name = "Cache will determine ping time for neighbor"
                    ) {
                        awaitItem()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }



    companion object {
        //Ping interval to use in testing - reduced to speed up test times
        const val PING_INTERVAL = 500L
    }
}