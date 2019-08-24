package com.ustadmobile.core.networkmanager

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.sharedse.network.DownloadJobItemManager
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TestDownloadJobItemManager {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var parentEntry: ContentEntry

    private lateinit var subLeaf: ContentEntry

    private lateinit var subLeafParentEntryJoin: ContentEntryParentChildJoin

    private lateinit var subCat: ContentEntry

    private lateinit var subCatParentEntryJoin: ContentEntryParentChildJoin

    private lateinit var subcatLeaf1: ContentEntry

    private lateinit var subcatLeaf1ParentEntryJoin: ContentEntryParentChildJoin

    private lateinit var subcatLeaf2: ContentEntry

    private lateinit var subcatLeaf2ParentEntryJoin: ContentEntryParentChildJoin

    private lateinit var subLeafContainer: Container

    private lateinit var subcatLeaf1Container: Container

    private lateinit var subcatLeaf2Container: Container

    private lateinit var downloadJob: DownloadJob

    private lateinit var subLeafDjItem: DownloadJobItem

    private lateinit var rootDjItem: DownloadJobItem

    private val context = Any()

    fun setupDb() {
        runBlocking {
            checkJndiSetup()
            db = UmAppDatabase.getInstance(context)
            repo = db//.getRepository("http://localhost/dummy/", "")
            db.clearAllTables()

            val entryDao = repo.contentEntryDao
            val entryParentDao = repo.contentEntryParentChildJoinDao
            parentEntry = ContentEntry("Parent entry", "parent", false, true)
            parentEntry.contentEntryUid = entryDao.insert(parentEntry)

            subLeaf = ContentEntry("Sub Leaf", "subleaf", true, true)
            subLeaf.contentEntryUid = entryDao.insert(subLeaf)
            subLeafParentEntryJoin = ContentEntryParentChildJoin(parentEntry, subLeaf, 0)
            subLeafParentEntryJoin.cepcjUid = entryParentDao.insert(subLeafParentEntryJoin)

            subCat = ContentEntry("Sub cat", "sub cat", false, true)
            subCat.contentEntryUid = entryDao.insert(subCat)
            subCatParentEntryJoin = ContentEntryParentChildJoin(parentEntry, subCat, 0)
            subCatParentEntryJoin.cepcjUid = entryParentDao.insert(subCatParentEntryJoin)

            subcatLeaf1 = ContentEntry("SubCat Leaf1", "subcatleaf1", true, true)
            subcatLeaf1.contentEntryUid = entryDao.insert(subcatLeaf1)
            subcatLeaf1ParentEntryJoin = ContentEntryParentChildJoin(subCat, subcatLeaf1, 0)
            subcatLeaf1ParentEntryJoin.cepcjUid = entryParentDao.insert(subcatLeaf1ParentEntryJoin)

            subcatLeaf2 = ContentEntry("SubCat Leaf2", "SubCat Leaf2", true, true)
            subcatLeaf2.contentEntryUid = entryDao.insert(subcatLeaf2)
            subcatLeaf2ParentEntryJoin = ContentEntryParentChildJoin(subCat, subcatLeaf2, 1)
            subcatLeaf2ParentEntryJoin.cepcjUid = entryParentDao.insert(subcatLeaf2ParentEntryJoin)

            //Create containers
            subLeafContainer = Container(subLeaf)
            subLeafContainer.fileSize = 1000
            subLeafContainer.containerUid = repo.containerDao.insert(subLeafContainer)

            subcatLeaf1Container = Container(subcatLeaf1)
            subcatLeaf1Container.fileSize = 1250
            subcatLeaf1Container.containerUid = repo.containerDao.insert(subcatLeaf1Container)

            subcatLeaf2Container = Container(subcatLeaf2)
            subcatLeaf2Container.fileSize = 1500
            subcatLeaf2Container.containerUid = repo.containerDao.insert(subcatLeaf2Container)


            downloadJob = DownloadJob()
            downloadJob.djRootContentEntryUid = parentEntry.contentEntryUid
            downloadJob.djUid = db.downloadJobDao.insert(downloadJob).toInt()
        }

    }


    @Throws(InterruptedException::class)
    protected fun setupRootAndSubleaf(manager: DownloadJobItemManager) {
        runBlocking {
            rootDjItem = DownloadJobItem(downloadJob,
                    parentEntry.contentEntryUid, 0, 0)
            manager.insertDownloadJobItems(listOf(rootDjItem))

            subLeafDjItem = DownloadJobItem(downloadJob,
                    subLeaf.contentEntryUid, subLeafContainer.containerUid,
                    subLeafContainer.fileSize)
            manager.insertDownloadJobItems(listOf(subLeafDjItem))

            manager.insertParentChildJoins(listOf(DownloadJobItemParentChildJoin(
                    rootDjItem!!.djiUid, subLeafDjItem!!.djiUid, 0)))
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadWithChildren_whenItemAdded_thenShouldIncreaseTotalLength() {
        runBlocking {
            UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenItemAdded_thenShouldIncreaseTotalLength")
            setupDb()
            val manager = DownloadJobItemManager(db, downloadJob!!.djUid, newSingleThreadContext("DownloadManager"))
            val latch = CountDownLatch(1)
            val statusRef = AtomicReference<DownloadJobItemStatus?>()
            manager.onDownloadJobItemChangeListener = {status, djUid ->
                statusRef.set(status)
                latch.countDown()
            }

            setupRootAndSubleaf(manager)

            latch.await(5, TimeUnit.SECONDS)

            Assert.assertNotNull(statusRef.get())
            Assert.assertEquals("Got an update for the root content entry uid",
                    parentEntry.contentEntryUid, statusRef.get()!!.contentEntryUid)
            Assert.assertEquals("Total size includes child item added",
                    subLeafContainer.fileSize, statusRef.get()!!.totalBytes)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch() {
        runBlocking {
            UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch")
            setupDb()
            val manager = DownloadJobItemManager(db, downloadJob.djUid, newSingleThreadContext("TestDownloadJobItemManager"))
            setupRootAndSubleaf(manager)

            manager.commit()

            Assert.assertEquals("Download root item size in database matches expected size",
                subLeafContainer.fileSize,
                db.downloadJobItemDao.findByContentEntryUid2(parentEntry.contentEntryUid)!!
                        .downloadLength)

        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenStatusLoaded_whenProgressRecorded_thenShouldFireEventForAllParents() {
        runBlocking {
            setupDb()
            val manager = DownloadJobItemManager(db, downloadJob.djUid,
                    newSingleThreadContext("TestDownloadJobItemManager"))
            setupRootAndSubleaf(manager)
            val parentStatusRef = AtomicReference<DownloadJobItemStatus>()
            val latch = CountDownLatch(1)

            manager.onDownloadJobItemChangeListener = {status, djUid ->
                if (status != null && status.contentEntryUid == parentEntry.contentEntryUid) {
                    parentStatusRef.set(status)
                    latch.countDown()
                }
            }


            manager.updateProgress(subLeafDjItem.djiUid, 300,
                    subLeafDjItem.downloadLength)
            latch.await(5, TimeUnit.SECONDS)

            Assert.assertNotNull("Got update to root entry item after updating parent",
                    parentStatusRef.get())

            Assert.assertEquals("Progress now includes update", 300,
                    parentStatusRef.get().bytesSoFar)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenStatusSavedToDatabase_whenReloaded_thenTotalsShouldMatch() {
        runBlocking {
            UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch")
            setupDb()
            val singleThreadContext = newSingleThreadContext("TestDownloadJobItemManager")
            val manager = DownloadJobItemManager(db, downloadJob.djUid, singleThreadContext)
            setupRootAndSubleaf(manager)

            manager.commit()

            val manager2 = DownloadJobItemManager(db, downloadJob.djUid, singleThreadContext)
            manager2.awaitLoaded()

            val manager2ContentEntryStatus = manager2
                    .findStatusByContentEntryUid(parentEntry.contentEntryUid)

            Assert.assertNotNull(manager2ContentEntryStatus)

            Assert.assertEquals("Parent entry total size is set as before",
                    subLeafContainer.fileSize, manager2ContentEntryStatus!!.totalBytes)
        }
    }


    private fun addItemsAndParents(numItems: Int): Map<Int, DownloadJobItemStatus> {
        val statusMap = HashMap<Int, DownloadJobItemStatus>()
        var lastItem: DownloadJobItemStatus? = null
        for (i in 0 until numItems) {
            val status = DownloadJobItemStatus()
            status.contentEntryUid = i.toLong()

            if (lastItem != null) {
                status.addParent(lastItem)
            }

            statusMap[i] = status
            lastItem = status
        }

        return statusMap
    }

    @Test
    fun given50000ObjectsCreated_whenMemoryCounted_memoryUsageShouldBeReasonable() {
        System.gc()
        val runtime = Runtime.getRuntime()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        val statusMap = addItemsAndParents(50000)
        System.gc()
        val memoryUsed = runtime.totalMemory() - runtime.freeMemory() - memoryBefore
        println("Map size = " + statusMap.size)
        Assert.assertTrue(memoryUsed < 1000 * 1000 * 10)//10MB
    }

    private fun findInMapByContentEntryUid(contentEntryUid: Long,
                                           map: Map<Int, DownloadJobItemStatus>): DownloadJobItemStatus? {
        for (status in map.values) {
            if (status.contentEntryUid == contentEntryUid)
                return status
        }

        return null
    }

    @Test
    fun given50000ObjectsCreated_whenGettingItemByContentEntryUid_retrievalTimeShouldBeReasonable() {
        System.gc()
        val statusMap = addItemsAndParents(50000)
        for (i in 0..9) {
            val entryUidToFind = (Math.random() * 50000).toInt().toLong()
            val startTime = System.currentTimeMillis()
            val itemFound = findInMapByContentEntryUid(entryUidToFind, statusMap)
            val lookupTime = System.currentTimeMillis() - startTime
            println("lookup time = " + lookupTime + "ms")
            Assert.assertNotNull("Found item in table", itemFound)
            Assert.assertTrue("Found item quickly enough", lookupTime < 50)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenParentWithChild_whenAllChildrenDownloadCompleted_thenParentStatusShouldBeCompleted() {
        runBlocking {
            checkJndiSetup()
            db = UmAppDatabase.getInstance(context)
            db.clearAllTables()

            downloadJob = DownloadJob()
            downloadJob.djRootContentEntryUid = 0
            downloadJob.djUid = db.downloadJobDao.insert(downloadJob).toInt()
            rootDjItem = DownloadJobItem(downloadJob.djUid, 0, 0, 0)
            val manager = DownloadJobItemManager(db, downloadJob.djUid, newSingleThreadContext("TestDownloadJobItemManager"))
            val childItems = LinkedList<DownloadJobItem>()
            for (i in 0..4) {
                val childItem = DownloadJobItem(downloadJob.djUid, (i + 1).toLong(),
                        (i + 1).toLong(), 500)
                childItems.add(childItem)
            }
            manager.insertDownloadJobItems(listOf(rootDjItem))
            manager.insertDownloadJobItems(childItems)

            val parentChildJoins = LinkedList<DownloadJobItemParentChildJoin>()
            var i = 1
            for (item in childItems) {
                parentChildJoins.add(DownloadJobItemParentChildJoin(rootDjItem.djiUid,
                        item.djiUid, i++.toLong()))
            }
            manager.insertParentChildJoins(parentChildJoins)

            for (item in childItems) {
                manager.updateStatus(item.djiUid, JobStatus.COMPLETE)
            }

            Assert.assertEquals("After all child items complete, root item status is completed",
                    JobStatus.COMPLETE, manager.rootItemStatus!!.status)
        }
    }

}
