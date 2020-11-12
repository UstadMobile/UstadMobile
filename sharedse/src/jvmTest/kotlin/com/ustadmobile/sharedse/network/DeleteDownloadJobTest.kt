package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DeleteDownloadJobTest{

    private lateinit var standAloneCommonContainerEntry: ContainerEntryWithContainerEntryFile
    private lateinit var commonFileContainerEntry: ContainerEntryWithContainerEntryFile
    private lateinit var zombieFileContainerEntry: ContainerEntryWithContainerEntryFile

    private lateinit var zombieFile: File
    private lateinit var commonFile: File

    private lateinit var containerTmpDir: File

    private lateinit var rootContentEntry: ContentEntry
    private lateinit var standAloneEntry: ContentEntry

    private var commonFilePath = "/com/ustadmobile/port/sharedse/container/testfile1.png"

    private var zombieFilePath = "/com/ustadmobile/port/sharedse/container/testfile2.png"

    lateinit var db: UmAppDatabase

    lateinit var containerDownloadManager: ContainerDownloadManager

    lateinit var downloadJob: DownloadJob

    lateinit var dwchildofparent: DownloadJobItem

    @Rule
    @JvmField
    var tmpFolderRule = TemporaryFolder()

    @Before
    fun setup(){

        db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()
        val repo = db.asRepository(Any(), "http://localhost/dummy", "",
                defaultHttpClient())

        containerTmpDir = tmpFolderRule.newFolder("clientContainerDir")

        commonFile = File(containerTmpDir, "testfile1.png")
        extractTestResourceToFile(commonFilePath, commonFile)

        zombieFile = File(containerTmpDir, "testfile2.png")
        extractTestResourceToFile(zombieFilePath, zombieFile)

        var dwItemDao = db.downloadJobItemDao
        var dwJoinDao = db.downloadJobItemParentChildJoinDao
        var statusDao = db.contentEntryStatusDao
        var dwDao = db.downloadJobDao

        downloadJob = DownloadJob()
        downloadJob.djUid = 1
        dwDao.insert(downloadJob)


        // standalone child - should not be deleted by test, has a file called abc
        var standaloneChild = DownloadJobItem()
        standaloneChild.djiUid = 3
        standaloneChild.timeStarted = 46366
        standaloneChild.djiContainerUid = 7
        standaloneChild.djiContentEntryUid = 3
        standaloneChild.djiDjUid = 1
        dwItemDao.insert(standaloneChild)

        standAloneEntry = ContentEntry()
        standAloneEntry.contentEntryUid = 3
        repo.contentEntryDao.insert(standAloneEntry)

        var containerOfStandAlone = Container()
        containerOfStandAlone.containerUid = standaloneChild.djiContainerUid
        containerOfStandAlone.containerContentEntryUid = standAloneEntry.contentEntryUid
        repo.containerDao.insert(containerOfStandAlone)

        var containerManager = ContainerManager(containerOfStandAlone, db, repo, containerTmpDir.path)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(commonFile, "testfile1.png"))
        }

        standAloneCommonContainerEntry = containerManager.getEntry("testfile1.png")!!

        var dwroot = DownloadJobItem()
        dwroot.djiUid = 1
        dwroot.timeStarted = 242353456
        dwroot.djiContentEntryUid = 1
        dwroot.djiDjUid = 1
        dwItemDao.insert(dwroot)

        rootContentEntry = ContentEntry()
        rootContentEntry.contentEntryUid = 1
        repo.contentEntryDao.insert(rootContentEntry)

        var rootStatus = ContentEntryStatus()
        rootStatus.cesUid = rootContentEntry.contentEntryUid
        statusDao.insert(rootStatus)

        var dwparent = DownloadJobItem()
        dwparent.djiUid = 2
        dwparent.timeStarted = 54446
        dwparent.djiContentEntryUid = 2
        dwparent.djiDjUid = 1
        dwItemDao.insert(dwparent)

        var parentEntry = ContentEntry()
        parentEntry.contentEntryUid = 2
        repo.contentEntryDao.insert(parentEntry)

        var parentEntryStatus = ContentEntryStatus()
        parentEntryStatus.cesUid = parentEntry.contentEntryUid
        statusDao.insert(parentEntryStatus)

        var rootParentJoin = DownloadJobItemParentChildJoin()
        rootParentJoin.djiParentDjiUid = dwroot.djiUid
        rootParentJoin.djiChildDjiUid =  dwparent.djiUid
        rootParentJoin.djiPcjUid = 1
        dwJoinDao.insert(rootParentJoin)

        dwchildofparent = DownloadJobItem()
        dwchildofparent.djiUid = 5
        dwchildofparent.timeStarted = 54545
        dwchildofparent.djiContentEntryUid = 5
        dwchildofparent.djiContainerUid = 8
        dwchildofparent.djiDjUid = 1
        dwItemDao.insert(dwchildofparent)

        var childOfParent = ContentEntry()
        childOfParent.contentEntryUid = 5
        repo.contentEntryDao.insert(childOfParent)

        var childOfparentStatus = ContentEntryStatus()
        childOfparentStatus.cesUid = childOfParent.contentEntryUid
        statusDao.insert(childOfparentStatus)

        var childOfParentJoin = DownloadJobItemParentChildJoin()
        childOfParentJoin.djiParentDjiUid = dwparent.djiUid
        childOfParentJoin.djiChildDjiUid = dwchildofparent.djiUid
        childOfParentJoin.djiPcjUid = 3
        dwJoinDao.insert(childOfParentJoin)

        var containerchildofparent = Container()
        containerchildofparent.containerUid = 8
        containerchildofparent.containerContentEntryUid = childOfParent.contentEntryUid
        repo.containerDao.insert(containerchildofparent)

        var parentContainerManager = ContainerManager(containerchildofparent, db, repo,
                containerTmpDir.path)
        runBlocking {
            parentContainerManager.addEntries(ContainerManager.FileEntrySource(zombieFile, "testfile2.png"))
            parentContainerManager.addEntries(ContainerManager.FileEntrySource(commonFile, "testfile1.png"))
        }

        commonFileContainerEntry = parentContainerManager.getEntry("testfile1.png")!!
        zombieFileContainerEntry = parentContainerManager.getEntry("testfile2.png")!!


        containerDownloadManager = mock()
    }

    @Test
    fun givenRootEntry_whenDeleted_checkAllChildrenDeleted(){
        runBlocking {
            val successful = deleteDownloadJob(db, downloadJob.djUid, containerDownloadManager) {
                println(it)
            }

            verifyBlocking(containerDownloadManager, timeout(5000)) {
                handleDownloadJobItemUpdated(argThat {
                    djiUid == dwchildofparent.djiUid && djiStatus == JobStatus.DELETED
                }, eq(false))
            }

            Assert.assertTrue("Delete job reports success", successful)
            Assert.assertTrue(File(commonFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertFalse(File(zombieFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
        }
    }

    @Test
    fun givenSingleEntry_whenDeleted_checkDeleted(){
        runBlocking {
            val successful = deleteDownloadJob(db, downloadJob.djUid, containerDownloadManager) {
                println(it)
            }


            Assert.assertTrue("Delete job reports success", successful)
            Assert.assertTrue(File(commonFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertFalse(File(zombieFileContainerEntry.containerEntryFile!!.cefPath!!).exists())

        }


    }


}