//package com.ustadmobile.core.networkmanager
//
//import com.nhaarman.mockitokotlin2.any
//import com.nhaarman.mockitokotlin2.timeout
//import com.nhaarman.mockitokotlin2.verify
//import com.ustadmobile.core.db.JobStatus
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UmResultCallback
//import com.ustadmobile.lib.db.entities.*
//import com.ustadmobile.test.core.impl.PlatformTestUtil
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mockito
//import java.io.IOException
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.atomic.AtomicReference
//
//class TestDownloadJobItemManagerList {
//
//    private val appDatabase: UmAppDatabase = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)
//
//    private val appDatabaseRepo: UmAppDatabase = appDatabase.getUmRepository("http://localhost/dummy/",
//            "")
//
//    private var downloadJob1: DownloadJob? = null
//
//    private var downloadJobItem1: DownloadJobItem? = null
//
//    var rootContentEntry1: ContentEntry? = null
//
//
//    @Before
//    fun initDb() {
//        appDatabase.clearAllTables()
//
//        rootContentEntry1 = ContentEntry("title", "desc", false, true)
//        rootContentEntry1?.contentEntryUid = appDatabaseRepo.contentEntryDao.insert(rootContentEntry1)
//
//        val rootContainer = Container(rootContentEntry1!!)
//        rootContainer.containerUid = appDatabaseRepo.containerDao.insert(rootContainer)
//
//        downloadJob1 = DownloadJob(rootContentEntry1!!.contentEntryUid, System.currentTimeMillis())
//
//        downloadJobItem1 = DownloadJobItem(downloadJob1!!, rootContentEntry1!!.contentEntryUid,
//                rootContainer.containerUid, 1000)
//
//    }
//
//    @Test
//    @Throws(IOException::class, InterruptedException::class)
//    fun givenListOfDownloadJobItemManagers_whenExistingContentEntryStatusRequested_thenShouldCallbackWithValue() {
//        val itemManagerList = DownloadJobItemManagerList(appDatabase)
//        val itemManager = itemManagerList.createNewDownloadJobItemManager(downloadJob1!!)
//        itemManager.insertDownloadJobItemsSync(listOf(downloadJobItem1!!))
//
//        val latch = CountDownLatch(1)
//        val resultRef = AtomicReference<DownloadJobItemStatus>()
//        itemManagerList.findDownloadJobItemStatusByContentEntryUid(rootContentEntry1!!.contentEntryUid,
//                object: UmResultCallback<DownloadJobItemStatus> {
//                    override fun onDone(result: DownloadJobItemStatus?) {
//                        resultRef.set(result)
//                        latch.countDown()
//                    }
//                })
//
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertNotNull("List callback will find an existing contententry and callback if in " +
//                "any downloadjobitemmanage", resultRef.get())
//    }
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenListOfDownloadManagers_whenNonExistingContentEntryStatusRequested_thenShouldCallbackWithNull() {
//        val itemManagerList = DownloadJobItemManagerList(appDatabase)
//        val itemManager = itemManagerList.createNewDownloadJobItemManager(downloadJob1!!)
//        itemManager.insertDownloadJobItemsSync(listOf(downloadJobItem1!!))
//
//        val latch = CountDownLatch(1)
//        val resultRef = AtomicReference<DownloadJobItemStatus>()
//        itemManagerList.findDownloadJobItemStatusByContentEntryUid(-1,
//                object: UmResultCallback<DownloadJobItemStatus> {
//                    override fun onDone(result: DownloadJobItemStatus?) {
//                        resultRef.set(result)
//                        latch.countDown()
//                    }
//                })
//
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertNull("List callback to find a non-existing contententry is null",
//                resultRef.get())
//    }
//
//
//    @Test
//    @Throws(InterruptedException::class)
//    fun givenDownloadJobItemManager_whenDownloadJobProgresses_thenEventIsFired() {
//        val itemManagerList = DownloadJobItemManagerList(appDatabase)
//        val itemManager = itemManagerList.createNewDownloadJobItemManager(downloadJob1!!)
//        itemManager.insertDownloadJobItemsSync(listOf(downloadJobItem1!!))
//        val mockListener = Mockito.mock(DownloadJobItemManager.OnDownloadJobItemChangeListener::class.java)
//        itemManagerList.addDownloadChangeListener(mockListener)
//
//        itemManager.updateProgress(downloadJobItem1!!.djiUid.toInt(), 50, 100)
//
//        verify(mockListener, timeout(5000)).onDownloadJobItemChange(any(), any())
//    }
//
//    @Test
//    fun givenDownloadJobItemManagerCreated_whenGetManagerCalled_thenShouldReturnManager() {
//        val itemManagerList = DownloadJobItemManagerList(appDatabase)
//        val itemManager = itemManagerList.createNewDownloadJobItemManager(downloadJob1!!)
//
//        Assert.assertEquals(itemManager, itemManagerList.getDownloadJobItemManager(downloadJob1!!
//                .djUid.toInt()))
//    }
//
//    @Test
//    fun givenDownloadJobItemManagerCreated_whenRootEntryIsCompleted_thenDownloadJobItemManagerShouldBeClosed() {
//        val itemManagerList = DownloadJobItemManagerList(appDatabase)
//        val itemManager = itemManagerList.createNewDownloadJobItemManager(downloadJob1!!)
//        itemManager.insertDownloadJobItemsSync(listOf(downloadJobItem1!!))
//
//        val itemManagerBeforeComplete = itemManagerList.getDownloadJobItemManager(downloadJob1!!.djUid.toInt())
//
//        val latch = CountDownLatch(1)
//        itemManager.updateStatus(downloadJobItem1!!.djiUid.toInt(), JobStatus.COMPLETE,
//                object: UmResultCallback<Void?> {
//                    override fun onDone(result: Void?) {
//                        latch.countDown()
//                    }
//                })
//
//        latch.await(5, TimeUnit.SECONDS)
//
//        Assert.assertNotNull("Before downloadjob was marked as completed, item manager was in list",
//                itemManagerBeforeComplete)
//        Assert.assertNull("Once downloadjob root entry is marked as complete, item manager was " +
//                "removed from the list",
//                itemManagerList.getDownloadJobItemManager(downloadJob1!!.djUid.toInt()))
//
//    }
//
//
//
//}