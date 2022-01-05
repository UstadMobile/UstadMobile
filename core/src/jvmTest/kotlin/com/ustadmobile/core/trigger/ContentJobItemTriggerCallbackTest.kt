package com.ustadmobile.core.trigger



import com.ustadmobile.core.db.ContentJobItemTriggersCallback
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
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

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE),
                randomUuid().toString())

        db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                .addSyncCallback(nodeIdAndAuth, true)
                .addCallback(ContentJobItemTriggersCallback())
                .build()
                .clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, true)

        contentJob = ContentJob().apply{
            cjUid = 1
        }
        parentJobItem = ContentJobItem().apply{
            cjiUid = 1
            cjiJobUid = contentJob.cjUid
            cjiStatus = JobStatus.COMPLETE
        }
        childJobItem = ContentJobItem().apply {
            cjiUid = 2
            cjiJobUid = contentJob.cjUid
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
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job queued", JobStatus.QUEUED, jobItem!!.cjiRecursiveStatus)
        }

    }

    @Test
    fun givenChildrenJobItemRunningJob_whenRecursiveStatusUpdated_thenParentShouldBeRunning(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.RUNNING)
            val parentJobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job running", JobStatus.RUNNING, parentJobItem!!.cjiRecursiveStatus)
        }


    }


    @Test
    fun givenChildrenJobCompleted_whenRecursiveStatusUpdated_thenJobParentShouldBeCompleted(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.COMPLETE)
            val childItem = db.contentJobItemDao.findByUidAsync(childJobItem.cjiUid)
            Assert.assertEquals("child job completed", JobStatus.COMPLETE, childItem!!.cjiRecursiveStatus)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job completed", JobStatus.COMPLETE, jobItem!!.cjiRecursiveStatus)
        }



    }

    @Test
    fun givenChildrenJobItemFailed_whenRecursiveStatusUpdated_thenJobParentShouldBeFailed(){

        runBlocking {
            db.contentJobItemDao.updateItemStatus(parentJobItem.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.FAILED)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job failed", JobStatus.FAILED, jobItem!!.cjiRecursiveStatus)
        }

    }

    @Test
    fun givenChildrenJobItemWaitingForConnection_whenRecursiveStatusUpdated_thenJobParentShouldBeWaitingForConnection(){


        runBlocking {
            db.contentJobItemDao.updateItemStatus(childJobItem.cjiUid, JobStatus.WAITING_FOR_CONNECTION)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job waiting for connection", JobStatus.WAITING_FOR_CONNECTION, jobItem!!.cjiRecursiveStatus)

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
            db.contentJobItemDao.updateItemStatus(child2.cjiUid, JobStatus.COMPLETE)
            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job partial failed", JobStatus.PARTIAL_FAILED, jobItem!!.cjiRecursiveStatus)
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
            db.contentJobItemDao.updateItemStatus(childFolder.cjiUid, JobStatus.COMPLETE)

            db.contentJobItemDao.updateItemStatus(child3.cjiUid, JobStatus.FAILED)
            db.contentJobItemDao.updateItemStatus(child4.cjiUid, JobStatus.COMPLETE)


            val childFolderItem = db.contentJobItemDao.findByUidAsync(childFolder.cjiUid)
            Assert.assertEquals("subfolder partial failed", JobStatus.PARTIAL_FAILED, childFolderItem!!.cjiRecursiveStatus)

            val jobItem = db.contentJobItemDao.findByUidAsync(parentJobItem.cjiUid)
            Assert.assertEquals("parent job partial failed", JobStatus.PARTIAL_FAILED, jobItem!!.cjiRecursiveStatus)
        }

    }




}