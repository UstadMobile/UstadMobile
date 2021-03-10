package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.util.UstadTestRule
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.junit.rules.TemporaryFolder
import java.io.File
import javax.naming.InitialContext

class DeleteDownloadJobTest{

    private lateinit var dwroot: DownloadJobItem
    private lateinit var dwchildofparent: DownloadJobItem
    private lateinit var standAloneCommonContainerEntry: ContainerEntryWithContainerEntryFile
    private lateinit var commonFileContainerEntry: ContainerEntryWithContainerEntryFile
    private lateinit var zombieFileContainerEntry: ContainerEntryWithContainerEntryFile

    private lateinit var zombieFile: File
    private lateinit var commonFile: File

    private lateinit var containerTmpDir: File

    private var commonFilePath = "/com/ustadmobile/port/sharedse/container/testfile1.png"

    private var zombieFilePath = "/com/ustadmobile/port/sharedse/container/testfile2.png"

    lateinit var db: UmAppDatabase
    lateinit var repo: UmAppDatabase

    lateinit var containerDownloadManager: ContainerDownloadManager


    @Rule
    @JvmField
    var tmpFolderRule = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var clientDi: DI

    lateinit var mockNetworkManager: NetworkManagerBle

    @Before
    fun setup(){
        val endpointScope = EndpointScope()
        mockNetworkManager = mock{}

        clientDi = DI {
            import(ustadTestRule.diModule)
            bind<NetworkManagerBle>() with singleton { mockNetworkManager }
            bind<ContainerDownloadManager>() with scoped(endpointScope).singleton {
                ContainerDownloadManagerImpl(endpoint = context, di = di)
            }
        }
        val cloudMockWebServer = MockWebServer()
        val accountManager: UstadAccountManager by clientDi.instance()
        accountManager.activeAccount = UmAccount(0, "guest", "",
                cloudMockWebServer.url("/").toString())

        db =  clientDi.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_DB)
        repo = clientDi.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        containerDownloadManager = clientDi.on(accountManager.activeAccount).direct.instance()

        containerTmpDir = tmpFolderRule.newFolder("clientContainerDir")

        commonFile = File(containerTmpDir, "testfile1.png")
        extractTestResourceToFile(commonFilePath, commonFile)

        zombieFile = File(containerTmpDir, "testfile2.png")
        extractTestResourceToFile(zombieFilePath, zombieFile)

        // start of standalone
        val standAloneEntry = ContentEntry()
        standAloneEntry.contentEntryUid = repo.contentEntryDao.insert(standAloneEntry)

        val containerOfStandAlone = Container()
        containerOfStandAlone.containerContentEntryUid = standAloneEntry.contentEntryUid
        containerOfStandAlone.containerUid = repo.containerDao.insert(containerOfStandAlone)

        val containerManager = ContainerManager(containerOfStandAlone, db, repo, containerTmpDir.path)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(commonFile, "testfile1.png"))
        }

        standAloneCommonContainerEntry = containerManager.getEntry("testfile1.png")!!

        val downloadJob = DownloadJob()
        downloadJob.djUid = db.downloadJobDao.insert(downloadJob).toInt()

        // standalone child - should not be deleted by test, has a file called abc
        val standaloneChild = DownloadJobItem()
        standaloneChild.timeStarted = 46366
        standaloneChild.djiContainerUid = containerOfStandAlone.containerUid
        standaloneChild.djiContentEntryUid = standAloneEntry.contentEntryUid
        standaloneChild.djiDjUid = downloadJob.djUid
        standaloneChild.djiUid = db.downloadJobItemDao.insert(standaloneChild).toInt()
        //end of standalone

        // start of root
        val rootContentEntry = ContentEntry()
        rootContentEntry.contentEntryUid = repo.contentEntryDao.insert(rootContentEntry)

        dwroot = DownloadJobItem()
        dwroot.timeStarted = 242353456
        dwroot.djiContentEntryUid = rootContentEntry.contentEntryUid
        dwroot.djiDjUid = downloadJob.djUid
        dwroot.djiUid = db.downloadJobItemDao.insert(dwroot).toInt()

        val parentEntry = ContentEntry()
        parentEntry.contentEntryUid = repo.contentEntryDao.insert(parentEntry)

        val dwparent = DownloadJobItem()
        dwparent.timeStarted = 54446
        dwparent.djiContentEntryUid = parentEntry.contentEntryUid
        dwparent.djiDjUid = downloadJob.djUid
        dwparent.djiUid = db.downloadJobItemDao.insert(dwparent).toInt()

        val rootParentJoin = DownloadJobItemParentChildJoin()
        rootParentJoin.djiParentDjiUid = dwroot.djiUid
        rootParentJoin.djiChildDjiUid =  dwparent.djiUid
        rootParentJoin.djiPcjUid = 1
        db.downloadJobItemParentChildJoinDao.insert(rootParentJoin)

        val childOfParent = ContentEntry()
        childOfParent.contentEntryUid = repo.contentEntryDao.insert(childOfParent)

        val containerchildofparent = Container()
        containerchildofparent.containerContentEntryUid = childOfParent.contentEntryUid
        containerchildofparent.containerUid = repo.containerDao.insert(containerchildofparent)

        dwchildofparent = DownloadJobItem()
        dwchildofparent.timeStarted = 54545
        dwchildofparent.djiContentEntryUid = childOfParent.contentEntryUid
        dwchildofparent.djiContainerUid = containerchildofparent.containerUid
        dwchildofparent.djiDjUid = downloadJob.djUid
        dwchildofparent.djiUid = db.downloadJobItemDao.insert(dwchildofparent).toInt()

        val childOfParentJoin = DownloadJobItemParentChildJoin()
        childOfParentJoin.djiParentDjiUid = dwparent.djiUid
        childOfParentJoin.djiChildDjiUid = dwchildofparent.djiUid
        childOfParentJoin.djiPcjUid = 3
        db.downloadJobItemParentChildJoinDao.insert(childOfParentJoin)

        var parentContainerManager = ContainerManager(containerchildofparent, db, repo,
                containerTmpDir.path)
        runBlocking {
            parentContainerManager.addEntries(ContainerManager.FileEntrySource(zombieFile, "testfile2.png"))
            parentContainerManager.addEntries(ContainerManager.FileEntrySource(commonFile, "testfile1.png"))
        }

        commonFileContainerEntry = parentContainerManager.getEntry("testfile1.png")!!
        zombieFileContainerEntry = parentContainerManager.getEntry("testfile2.png")!!

    }

    @Test
    fun givenRootEntry_whenDeleted_checkAllChildrenDeleted(){
        runBlocking {
            val successful = containerDownloadManager.deleteDownloadJobItem(dwroot.djiUid) {
                println(it)
            }

            Assert.assertTrue("Delete job reports success", successful)
            Assert.assertTrue(File(commonFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertFalse(File(zombieFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertTrue(File(standAloneCommonContainerEntry.containerEntryFile!!.cefPath!!).exists())
        }
    }

    @Test
    fun givenSingleEntry_whenDeleted_checkDeleted(){

        runBlocking {
            val successful = containerDownloadManager.deleteDownloadJobItem(dwchildofparent.djiUid) {
                println(it)
            }

            Assert.assertTrue("Delete job reports success", successful)
            Assert.assertTrue(File(commonFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertFalse(File(zombieFileContainerEntry.containerEntryFile!!.cefPath!!).exists())
            Assert.assertTrue(File(standAloneCommonContainerEntry.containerEntryFile!!.cefPath!!).exists())

        }
    }




}