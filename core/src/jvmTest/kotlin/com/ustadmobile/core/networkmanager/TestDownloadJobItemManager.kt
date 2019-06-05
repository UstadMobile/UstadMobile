//package com.ustadmobile.core.networkmanager
//
//import com.ustadmobile.core.db.JobStatus
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.db.dao.ContentEntryDao
//import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
//import com.ustadmobile.core.impl.UMLog
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.lib.db.entities.Container
//import com.ustadmobile.lib.db.entities.ContentEntry
//import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
//import com.ustadmobile.lib.db.entities.DownloadJob
//import com.ustadmobile.lib.db.entities.DownloadJobItem
//import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
//import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
//import com.ustadmobile.port.sharedse.networkmanager.DownloadJobItemManager
//import com.ustadmobile.test.core.impl.PlatformTestUtil
//import com.ustadmobile.test.core.util.checkJndiSetup
//
//import org.junit.Assert
//import org.junit.Test
//
//import java.util.Arrays
//import java.util.Collections
//import java.util.HashMap
//import java.util.LinkedList
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.atomic.AtomicReference
//
//class TestDownloadJobItemManager {
//
//    private var db: UmAppDatabase? = null
//
//    private var repo: UmAppDatabase? = null
//
//    private var parentEntry: ContentEntry? = null
//
//    private var subLeaf: ContentEntry? = null
//
//    private var subLeafParentEntryJoin: ContentEntryParentChildJoin? = null
//
//    private var subCat: ContentEntry? = null
//
//    private var subCatParentEntryJoin: ContentEntryParentChildJoin? = null
//
//    private var subcatLeaf1: ContentEntry? = null
//
//    private var subcatLeaf1ParentEntryJoin: ContentEntryParentChildJoin? = null
//
//    private var subcatLeaf2: ContentEntry? = null
//
//    private var subcatLeaf2ParentEntryJoin: ContentEntryParentChildJoin? = null
//
//    private var subLeafContainer: Container? = null
//
//    private var subcatLeaf1Container: Container? = null
//
//    private var subcatLeaf2Container: Container? = null
//
//    private var downloadJob: DownloadJob? = null
//
//    private var subLeafDjItem: DownloadJobItem? = null
//
//    private var rootDjItem: DownloadJobItem? = null
//
//    private val context = Any()
//
//    fun setupDb() {
//        checkJndiSetup()
//        db = UmAppDatabase.getInstance(context)
//        repo = db!!//.getRepository("http://localhost/dummy/", "")
//        db!!.clearAllTables()
//
//        val entryDao = repo!!.contentEntryDao
//        val entryParentDao = repo!!.contentEntryParentChildJoinDao
//        parentEntry = ContentEntry("Parent entry", "parent", false, true)
//        parentEntry!!.contentEntryUid = entryDao.insert(parentEntry!!)
//
//        subLeaf = ContentEntry("Sub Leaf", "subleaf", true, true)
//        subLeaf!!.contentEntryUid = entryDao.insert(subLeaf)
//        subLeafParentEntryJoin = ContentEntryParentChildJoin(parentEntry!!, subLeaf!!, 0)
//        subLeafParentEntryJoin!!.cepcjUid = entryParentDao.insert(subLeafParentEntryJoin)
//
//        subCat = ContentEntry("Sub cat", "sub cat", false, true)
//        subCat!!.contentEntryUid = entryDao.insert(subCat)
//        subCatParentEntryJoin = ContentEntryParentChildJoin(parentEntry!!, subCat!!, 0)
//        subCatParentEntryJoin!!.cepcjUid = entryParentDao.insert(subCatParentEntryJoin)
//
//        subcatLeaf1 = ContentEntry("SubCat Leaf1", "subcatleaf1", true, true)
//        subcatLeaf1!!.contentEntryUid = entryDao.insert(subcatLeaf1)
//        subcatLeaf1ParentEntryJoin = ContentEntryParentChildJoin(subCat!!, subcatLeaf1!!, 0)
//        subcatLeaf1ParentEntryJoin!!.cepcjUid = entryParentDao.insert(subcatLeaf1ParentEntryJoin)
//
//        subcatLeaf2 = ContentEntry("SubCat Leaf2", "SubCat Leaf2", true, true)
//        subcatLeaf2!!.contentEntryUid = entryDao.insert(subcatLeaf2)
//        subcatLeaf2ParentEntryJoin = ContentEntryParentChildJoin(subCat!!, subcatLeaf2!!, 1)
//        subcatLeaf2ParentEntryJoin!!.cepcjUid = entryParentDao.insert(subcatLeaf2ParentEntryJoin)
//
//        //Create containers
//        subLeafContainer = Container(subLeaf!!)
//        subLeafContainer!!.fileSize = 1000
//        subLeafContainer!!.containerUid = repo!!.containerDao.insert(subLeafContainer)
//
//        subcatLeaf1Container = Container(subcatLeaf1!!)
//        subcatLeaf1Container!!.fileSize = 1250
//        subcatLeaf1Container!!.containerUid = repo!!.containerDao.insert(subcatLeaf1Container)
//
//        subcatLeaf2Container = Container(subcatLeaf2!!)
//        subcatLeaf2Container!!.fileSize = 1500
//        subcatLeaf2Container!!.containerUid = repo!!.containerDao.insert(subcatLeaf2Container)
//
//
//        downloadJob = DownloadJob()
//        downloadJob!!.djRootContentEntryUid = parentEntry!!.contentEntryUid
//        downloadJob!!.djUid = db!!.downloadJobDao.insert(downloadJob!!)
//    }
//
//
//    @Throws(InterruptedException::class)
//    protected fun setupRootAndSubleaf(manager: DownloadJobItemManager) {
//        val latch = CountDownLatch(2)
//        rootDjItem = DownloadJobItem(downloadJob!!,
//                parentEntry!!.contentEntryUid, 0, 0)
//        manager.insertDownloadJobItems(Arrays.asList<T>(rootDjItem!!), { aVoid -> latch.countDown() })
//
//        subLeafDjItem = DownloadJobItem(downloadJob!!,
//                subLeaf!!.contentEntryUid, subLeafContainer!!.containerUid,
//                subLeafContainer!!.fileSize)
//        manager.insertDownloadJobItems(Arrays.asList<T>(subLeafDjItem!!), { aVoid -> latch.countDown() })
//
//        latch.await(5, TimeUnit.SECONDS)
//
//        val latch2 = CountDownLatch(1)
//        manager.insertParentChildJoins(Arrays.asList<T>(DownloadJobItemParentChildJoin(
//                rootDjItem!!.djiUid, subLeafDjItem!!.djiUid, 0)),
//                { aVoid -> latch2.countDown() })
//        latch2.await(5, TimeUnit.SECONDS)
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenDownloadWithChildren_whenItemAdded_thenShouldIncreaseTotalLength() {
//        UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenItemAdded_thenShouldIncreaseTotalLength")
//        setupDb()
//        val manager = DownloadJobItemManager(db, downloadJob!!.djUid)
//        val latch = CountDownLatch(1)
//        val statusRef = AtomicReference<DownloadJobItemStatus>()
//        manager.setOnDownloadJobItemChangeListener({ status, manager1 ->
//            if (status.getContentEntryUid() === downloadJob!!.djRootContentEntryUid && status.getTotalBytes() === subLeafContainer!!.fileSize) {
//                statusRef.set(status)
//                latch.countDown()
//            }
//
//        })
//
//        setupRootAndSubleaf(manager)
//
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertNotNull(statusRef.get())
//        Assert.assertEquals("Got an update for the root content entry uid",
//                parentEntry!!.contentEntryUid, statusRef.get().contentEntryUid)
//        Assert.assertEquals("Total size includes child item added",
//                subLeafContainer!!.fileSize, statusRef.get().totalBytes)
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch() {
//        UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch")
//        setupDb()
//        val manager = DownloadJobItemManager(db, downloadJob!!.djUid)
//        setupRootAndSubleaf(manager)
//
//        val latch = CountDownLatch(1)
//        manager.commit({ aVoid -> latch.countDown() })
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertEquals("Download root item size in database matches expected size",
//                subLeafContainer!!.fileSize,
//                db!!.downloadJobItemDao.findByContentEntryUid2(parentEntry!!.contentEntryUid)!!
//                        .downloadLength)
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenStatusLoaded_whenProgressRecorded_thenShouldFireEventForAllParents() {
//        setupDb()
//        val manager = DownloadJobItemManager(db, downloadJob!!.djUid)
//        setupRootAndSubleaf(manager)
//        val parentStatusRef = AtomicReference<DownloadJobItemStatus>()
//        val latch = CountDownLatch(1)
//        manager.setOnDownloadJobItemChangeListener({ status, manager2 ->
//            if (status != null && status!!.getContentEntryUid() === parentEntry!!.contentEntryUid) {
//                parentStatusRef.set(status)
//                latch.countDown()
//            }
//        })
//
//        manager.updateProgress(subLeafDjItem!!.djiUid, 300,
//                subLeafDjItem!!.downloadLength)
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertNotNull("Got update to root entry item after updating parent",
//                parentStatusRef.get())
//
//        Assert.assertEquals("Progress now includes update", 300,
//                parentStatusRef.get().bytesSoFar)
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenStatusSavedToDatabase_whenReloaded_thenTotalsShouldMatch() {
//        UMLog.l(UMLog.INFO, 420, "Test: " + "givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch")
//        setupDb()
//        var manager: DownloadJobItemManager? = DownloadJobItemManager(db, downloadJob!!.djUid)
//        setupRootAndSubleaf(manager)
//
//        val latch = CountDownLatch(1)
//        manager!!.commit({ aVoid -> latch.countDown() })
//        latch.await(5, TimeUnit.SECONDS)
//        manager = null
//
//
//        val latch2 = CountDownLatch(1)
//        val manager2 = DownloadJobItemManager(db, downloadJob!!.djUid)
//        latch2.await(5, TimeUnit.SECONDS)
//
//        val latch3 = CountDownLatch(1)
//        val statusRef = AtomicReference<DownloadJobItemStatus>()
//        manager2.findStatusByContentEntryUid(parentEntry!!.contentEntryUid, { status ->
//            statusRef.set(status)
//            latch3.countDown()
//        })
//
//        latch3.await(5, TimeUnit.SECONDS)
//
//        Assert.assertEquals("Parent entry total size is set as before",
//                subLeafContainer!!.fileSize,
//                statusRef.get().totalBytes)
//    }
//
//
//    private fun addItemsAndParents(numItems: Int): Map<Int, DownloadJobItemStatus> {
//        val statusMap = HashMap<Int, DownloadJobItemStatus>()
//        var lastItem: DownloadJobItemStatus? = null
//        for (i in 0 until numItems) {
//            val status = DownloadJobItemStatus()
//            status.contentEntryUid = i
//
//            if (lastItem != null) {
//                status.addParent(lastItem)
//            }
//
//            statusMap[i] = status
//            lastItem = status
//        }
//
//        return statusMap
//    }
//
//    @Test
//    fun given50000ObjectsCreated_whenMemoryCounted_memoryUsageShouldBeReasonable() {
//        System.gc()
//        val runtime = Runtime.getRuntime()
//        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
//        val statusMap = addItemsAndParents(50000)
//        System.gc()
//        val memoryUsed = runtime.totalMemory() - runtime.freeMemory() - memoryBefore
//        println("Map size = " + statusMap.size)
//        Assert.assertTrue(memoryUsed < 1000 * 1000 * 10)//10MB
//    }
//
//    private fun findInMapByContentEntryUid(contentEntryUid: Long,
//                                           map: Map<Int, DownloadJobItemStatus>): DownloadJobItemStatus? {
//        for (status in map.values) {
//            if (status.contentEntryUid == contentEntryUid)
//                return status
//        }
//
//        return null
//    }
//
//    @Test
//    fun given50000ObjectsCreated_whenGettingItemByContentEntryUid_retrievalTimeShouldBeReasonable() {
//        System.gc()
//        val statusMap = addItemsAndParents(50000)
//        for (i in 0..9) {
//            val entryUidToFind = (Math.random() * 50000).toInt().toLong()
//            val startTime = System.currentTimeMillis()
//            val itemFound = findInMapByContentEntryUid(entryUidToFind, statusMap)
//            val lookupTime = System.currentTimeMillis() - startTime
//            println("lookup time = " + lookupTime + "ms")
//            Assert.assertNotNull("Found item in table", itemFound)
//            Assert.assertTrue("Found item quickly enough", lookupTime < 50)
//        }
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenParentWithChild_whenAllChildrenDownloadCompleted_thenParentStatusShouldBeCompleted() {
//        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
//        db!!.clearAllTables()
//
//        downloadJob = DownloadJob()
//        downloadJob!!.djRootContentEntryUid = 0
//        downloadJob!!.djUid = db!!.downloadJobDao.insert(downloadJob!!)
//        rootDjItem = DownloadJobItem(downloadJob!!.djUid, 0, 0, 0)
//        val manager = DownloadJobItemManager(db, downloadJob!!.djUid)
//        val childItems = LinkedList<DownloadJobItem>()
//        for (i in 0..4) {
//            val childItem = DownloadJobItem(downloadJob!!.djUid, (i + 1).toLong(),
//                    (i + 1).toLong(), 500)
//            childItems.add(childItem)
//        }
//        manager.insertDownloadJobItemsSync(listOf<T>(rootDjItem))
//        manager.insertDownloadJobItemsSync(childItems)
//        val parentChildJoins = LinkedList<DownloadJobItemParentChildJoin>()
//        var i = 1
//        for (item in childItems) {
//            parentChildJoins.add(DownloadJobItemParentChildJoin(rootDjItem!!.djiUid,
//                    item.djiUid, i++.toLong()))
//        }
//        val latch = CountDownLatch(1)
//        manager.insertParentChildJoins(parentChildJoins, { aVoid -> latch.countDown() })
//        latch.await(5, TimeUnit.SECONDS)
//
//        val statusLatch = CountDownLatch(childItems.size)
//        for (item in childItems) {
//            manager.updateStatus(item.djiUid, JobStatus.COMPLETE,
//                    { aVoid -> statusLatch.countDown() })
//        }
//        statusLatch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertEquals("After all child items complete, root item status is completed",
//                JobStatus.COMPLETE, manager.getRootItemStatus().getStatus())
//    }
//
//
//}
