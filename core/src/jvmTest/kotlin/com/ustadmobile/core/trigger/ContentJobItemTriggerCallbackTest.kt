package com.ustadmobile.core.trigger



import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.random.Random


class ContentJobItemTriggerCallbackTest {

    lateinit var db: UmAppDatabase

    lateinit var contentJob: ContentJob

    lateinit var parentJobItem: ContentJobItem

    lateinit var childJobItem: ContentJobItem

    @Before
    fun setup(){

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE),
                randomUuid().toString())

        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                "jdbc:sqlite:build/tmp/UmAppDatabaseTriggerTest.sqlite")
                .addSyncCallback(nodeIdAndAuth)
                .addCallback(ContentJobItemTriggersCallback())
                .build()
                .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)

        contentJob = ContentJob().apply{
            cjUid = 1
        }
        parentJobItem = ContentJobItem().apply{
            cjiUid = 1
            cjiJobUid = contentJob.cjUid
            cjiItemProgress = 100
            cjiItemTotal = 100
            cjiStatus = JobStatus.COMPLETE
        }
        childJobItem = ContentJobItem().apply {
            cjiUid = 2
            cjiJobUid = contentJob.cjUid
            cjiItemTotal = 100
            cjiItemProgress = 0
            cjiParentCjiUid = parentJobItem.cjiUid
            cjiStatus = JobStatus.QUEUED
        }

        runBlocking {
            db.contentJobDao.insertAsync(contentJob)
            db.contentJobItemDao.insertJobItem(parentJobItem)
            db.contentJobItemDao.insertJobItem(childJobItem)
        }
    }

    @Test
    fun givenChildrenJobInQueue_whenRecursiveStatusUpdated_thenJobParentShouldBeQueued(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.QUEUED)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 0, 100)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 100, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 200, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job queued", JobStatus.QUEUED, jobItem.cjiRecursiveStatus)
        }

    }

    @Test
    fun givenChildrenJobItemRunningJob_whenRecursiveStatusUpdated_thenParentShouldBeRunning(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.RUNNING)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 25, 100)
            val parentJobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 125, parentJobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 200, parentJobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job running", JobStatus.RUNNING, parentJobItem.cjiRecursiveStatus)
        }

    }


    @Test
    fun givenChildrenJobCompleted_whenRecursiveStatusUpdated_thenJobParentShouldBeCompleted(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.COMPLETE)
            val childItem = db.contentJobItemDao.findByUidAsync(childJobItem.cjiUid)
            Assert.assertEquals("child job completed", JobStatus.COMPLETE, childItem!!.cjiRecursiveStatus)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 100, 100)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 200, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 200, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job completed", JobStatus.COMPLETE, jobItem.cjiRecursiveStatus)
        }

    }

    @Test
    fun givenChildrenJobItemFailed_whenRecursiveStatusUpdated_thenJobParentShouldBeFailed(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(parentJobItem.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 90, 100)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 190, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 200, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job failed", JobStatus.FAILED, jobItem.cjiRecursiveStatus)
        }

    }

    @Test
    fun givenChildrenJobItemWaitingForConnection_whenRecursiveStatusUpdated_thenJobParentShouldBeWaitingForConnection(){


        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.WAITING_FOR_CONNECTION)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 50, 100)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 150, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 200, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job waiting for connection", JobStatus.WAITING_FOR_CONNECTION, jobItem.cjiRecursiveStatus)
        }


    }

    @Test
    fun givenChildrenJobItemCompletedAndFailed_whenRecursiveStatusUpdated_thenJobParentShouldBePartialFailed(){

        val child2 = ContentJobItem().apply {
            cjiUid = 3
            cjiJobUid = contentJob.cjUid
            cjiParentCjiUid = parentJobItem.cjiUid
            cjiStatus = JobStatus.QUEUED
        }

        runBlocking {
            db.contentJobItemDao.insertJobItem(child2)
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 75, 100)
            db.contentJobItemDao.updateItemStatus(child2.cjiUid, JobStatus.COMPLETE)
            db.contentJobItemDao.updateItemProgress(child2.cjiUid, 100, 100)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 275, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 300, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job partial failed", JobStatus.PARTIAL_FAILED, jobItem.cjiRecursiveStatus)
        }

    }



    @Test
    fun givenJobHasSubFoldersAndOneJobItemFailed_whenRecursiveStatusUpdated_thenParentOfSubFolderShouldBePartialFailed(){


        val childFolder = ContentJobItem().apply {
            cjiUid = 3
            cjiJobUid = contentJob.cjUid
            cjiParentCjiUid = parentJobItem.cjiUid
            cjiStatus = JobStatus.QUEUED
        }

        val child3 = ContentJobItem().apply {
            cjiUid = 4
            cjiJobUid = contentJob.cjUid
            cjiParentCjiUid = childFolder.cjiUid
            cjiStatus = JobStatus.QUEUED
        }

        val child4 = ContentJobItem().apply {
            cjiUid = 5
            cjiJobUid = contentJob.cjUid
            cjiParentCjiUid = childFolder.cjiUid
            cjiStatus = JobStatus.QUEUED
        }


        runBlocking {
            db.contentJobItemDao.insertJobItems(listOf(childFolder, child3, child4))
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.COMPLETE)
            db.contentJobItemDao.updateItemProgress(childJobItem.cjiUid, 100, 100)
            db.contentJobItemDao.updateItemStatus(childFolder.cjiUid, JobStatus.COMPLETE)
            db.contentJobItemDao.updateItemProgress(childFolder.cjiUid, 100, 100)

            db.contentJobItemDao.updateItemStatus(child3.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemProgress(child3.cjiUid, 25, 100)
            db.contentJobItemDao.updateItemStatus(child4.cjiUid, JobStatus.COMPLETE)
            db.contentJobItemDao.updateItemProgress(child4.cjiUid, 100, 100)


            val childFolderItem = db.contentJobItemDao.findByUidAsync(childFolder.cjiUid)!!
            Assert.assertEquals("subfolder job progress", 225, childFolderItem.cjiRecursiveProgress)
            Assert.assertEquals("subfolder job total", 300, childFolderItem.cjiRecursiveTotal)
            Assert.assertEquals("subfolder partial failed", JobStatus.PARTIAL_FAILED, childFolderItem.cjiRecursiveStatus)

            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job progress", 425, jobItem.cjiRecursiveProgress)
            Assert.assertEquals("parent job total", 500, jobItem.cjiRecursiveTotal)
            Assert.assertEquals("parent job partial failed", JobStatus.PARTIAL_FAILED, jobItem.cjiRecursiveStatus)
        }

    }



    @Test
    fun givenAllChildItemsCanceled_whenRecursiveStatusUpdated_thenStatusShouldBeCanceled(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(parentJobItem.cjiUid, JobStatus.CANCELED)
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.CANCELED)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)!!
            Assert.assertEquals("parent job status is canceled", JobStatus.CANCELED,
                jobItem.cjiRecursiveStatus)
        }

    }


    @Test
    fun dumpSqlStatements() {
        ContentJobItemTriggersCallback.dumpSqlStatements()
    }

}