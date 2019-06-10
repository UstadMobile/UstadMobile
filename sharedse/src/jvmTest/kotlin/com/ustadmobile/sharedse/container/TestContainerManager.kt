package com.ustadmobile.sharedse.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class TestContainerManager  {

    val testFileNames = listOf("testfile1.png", "testfile2.png")

    lateinit var db: UmAppDatabase

    lateinit var repo: UmAppDatabase

    val context = Any()

    val tmpFilesToDelete = mutableListOf<File>()

    lateinit var containerTmpDir: File

    lateinit var testFiles: MutableList<File>

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.Companion.getInstance(context)
        repo = db
        db.clearAllTables()

        containerTmpDir = File.createTempFile("testcontainerdir", "tmp")
        containerTmpDir .delete()
        containerTmpDir .mkdir()

        var fCount = 0
        testFiles = mutableListOf()
        testFileNames.forEach {
            val tmpFile = File.createTempFile("testContainerMgr", "testFile${fCount++}")
            extractTestResourceToFile("/com/ustadmobile/port/sharedse/container/$it",
                    tmpFile)
            testFiles.add(tmpFile)
        }

        tmpFilesToDelete.addAll(testFiles)

    }

    @After
    fun cleanup(){
        tmpFilesToDelete.forEach { it.delete() }
        tmpFilesToDelete.clear()
        containerTmpDir.deleteRecursively()
    }

    @Test
    fun `Given file entry added WHEN getInputStream called THEN should return input`() {
        val container = Container()
        container.containerUid = db.containerDao.insert(container)


        val containerManager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)
        runBlocking {
            containerManager.addEntries(null,
                    ContainerManager.FileEntrySource(testFiles[0], "testfile1.png"))

            val containerInStream = containerManager.getInputStream(containerManager.getEntry("testfile1.png")!!)
            containerInStream.use {
                Assert.assertArrayEquals("Stream provided by container is the same as provided by file",
                        testFiles[0].readBytes(), UMIOUtils.readStreamToByteArray(containerInStream))
            }
        }

    }

//    @Test
//    fun givenSameFileAddedToMultipleContainers_whenGetFileCalled_thenShouldReturnSameContainerEntryFile() {
//        val container1 = Container()
//        container1.containerUid = repo.containerDao.insert(container1)
//        val manager1 = ContainerManager(container1, db, repo,
//                containerTmpDir.getAbsolutePath())
//        runBlocking {
//
//        }
//
//
//        val filesToAdd1 = HashMap<File, String>()
//
//
//
//        manager1.addEntries(filesToAdd1, true)
//
//        val container2 = Container()
//        container2.containerUid = dbRepo.containerDao.insert(container2)
//        val manager2 = ContainerManager(container2, db, dbRepo,
//                tmpDir.getAbsolutePath())
//
//        val filesToAdd2 = HashMap<File, String>()
//        filesToAdd2[testFiles.get(0)] = "testfileothername.png"
//        filesToAdd2[testFiles.get(1)] = "anotherimage.png"
//        manager2.addEntries(filesToAdd2, true)
//
//        Assert.assertEquals("When two identical files are added, the same content entry file is used",
//                manager1.getEntry("testfile1.png")!!.ceCefUid,
//                manager2.getEntry("testfileothername.png")!!.ceCefUid)
//
//        Assert.assertNull("Manager2 does not return a container entry if given a name from manager1",
//                manager2.getEntry("testfile1.png"))
//
//        Assert.assertEquals("Cotnainer2 num entries = 2", 2,
//                dbRepo.containerDao.findByUid(container2.containerUid)!!.cntNumEntries.toLong())
//    }

}