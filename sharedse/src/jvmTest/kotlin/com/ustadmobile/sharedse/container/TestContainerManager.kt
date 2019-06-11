package com.ustadmobile.sharedse.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.ustadmobile.lib.util.getSystemTimeInMillis


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
            containerManager.addEntries(
                    ContainerManager.FileEntrySource(testFiles[0], "testfile1.png"))

            val containerInStream = containerManager.getInputStream(containerManager.getEntry("testfile1.png")!!)
            containerInStream.use {
                Assert.assertArrayEquals("Stream provided by container is the same as provided by file",
                        testFiles[0].readBytes(), UMIOUtils.readStreamToByteArray(containerInStream))
            }
        }

    }

    @Test
    fun givenSameFileAddedToMultipleContainers_whenGetFileCalled_thenShouldReturnSameContainerEntryFile() {
        runBlocking {
            val container1 = Container()
            container1.containerUid = repo.containerDao.insert(container1)
            val manager1 = ContainerManager(container1, db, repo,
                    containerTmpDir.getAbsolutePath())
            manager1.addEntries(ContainerManager.FileEntrySource(testFiles[0], "testfile1.png"))

            val container2 = Container()
            container2.containerUid = repo.containerDao.insert(container2)
            val manager2 = ContainerManager(container2, db, repo,
                    containerTmpDir.getAbsolutePath())

            val filesToAdd2 = HashMap<File, String>()
            filesToAdd2[testFiles.get(0)] = "testfileothername.png"
            filesToAdd2[testFiles.get(1)] = "anotherimage.png"
            manager2.addEntries(
                    ContainerManager.FileEntrySource(testFiles[0], "testfileothername.png"),
                    ContainerManager.FileEntrySource(testFiles[1], "anotherimage.png"))

            Assert.assertEquals("When two identical files are added, the same content entry file is used",
                    manager1.getEntry("testfile1.png")!!.ceCefUid,
                    manager2.getEntry("testfileothername.png")!!.ceCefUid)

            Assert.assertNull("Manager2 does not return a container entry if given a name from manager1",
                    manager2.getEntry("testfile1.png"))

            Assert.assertEquals("Cotnainer2 num entries = 2", 2,
                    repo.containerDao.findByUid(container2.containerUid)!!.cntNumEntries.toLong())
        }
    }

    @Test
    fun givenExistingContainer_whenCopyToNewContainerCalled_thenShouldHaveSameContents() {
        runBlocking {
            val container = Container()
            container.containerUid = repo.containerDao.insert(container)
            val manager = ContainerManager(container, db, repo, containerTmpDir.getAbsolutePath())

            manager.addEntries(ContainerManager.FileEntrySource(testFiles[0], "testfileothername.png"),
                    ContainerManager.FileEntrySource(testFiles[1], "anotherimage.png"))

            val copy = manager.copyToNewContainer()
            Assert.assertArrayEquals(UMIOUtils.readStreamToByteArray(manager.getInputStream(
                    manager.getEntry("testfileothername.png")!!)),
                    UMIOUtils.readStreamToByteArray(copy.getInputStream(
                            copy.getEntry("testfileothername.png")!!)))
            Assert.assertArrayEquals(UMIOUtils.readStreamToByteArray(manager.getInputStream(
                    manager.getEntry("anotherimage.png")!!)),
                    UMIOUtils.readStreamToByteArray(copy.getInputStream(
                            copy.getEntry("anotherimage.png")!!)))
        }

    }

    @Test
    @Throws(IOException::class)
    fun givenExistingContainer_whenFileAddedWithSamePath_thenFileShouldBeOverwritten() {
        runBlocking {
            val container = Container()
            container.containerUid = repo.containerDao.insert(container)
            val manager = ContainerManager(container, db, repo, containerTmpDir.getAbsolutePath())

            val version1Content = "Version-1"
            val version2Content = "Version-2"

            val entryv1 = File.createTempFile("tmp", "testv1")
            val foutV1 = FileOutputStream(entryv1)
            UMIOUtils.readFully(ByteArrayInputStream(version1Content.toByteArray()), foutV1)
            foutV1.close()

            val entryV2 = File.createTempFile("tmp", "testv2")
            val foutV2 = FileOutputStream(entryV2)
            UMIOUtils.readFully(ByteArrayInputStream(version2Content.toByteArray()), foutV2)
            foutV2.close()

            manager.addEntries(ContainerManager.FileEntrySource(entryv1, "test.txt"))

            val v1ContentFromContainer = UMIOUtils.readStreamToByteArray(manager.getInputStream(
                    manager.getEntry("test.txt")!!))

            manager.addEntries(ContainerManager.FileEntrySource(entryV2, "test.txt"))
            val v2ContentFRomContainer = UMIOUtils.readStreamToByteArray(manager.getInputStream(
                    manager.getEntry("test.txt")!!))


            Assert.assertArrayEquals("After adding first version, got version 1 content",
                    version1Content.toByteArray(), v1ContentFromContainer)

            Assert.assertArrayEquals("After adding second version, got version 2 content",
                    version2Content.toByteArray(), v2ContentFRomContainer)
            Assert.assertEquals("After adding an entry with the same name, there is still only one entry",
                    1, db.containerEntryDao.findByContainer(container.containerUid).size)
        }



    }


}