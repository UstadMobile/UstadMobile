package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.*
import java.util.zip.GZIPInputStream


class TestContainerManager {

    private val testFileNames = listOf("testfile1.png", "testfile2.png")

    private val testGzipFiles = listOf("testfile2.png", "BigBuckBunny.mp4")

    lateinit var db: UmAppDatabase

    lateinit var repo: UmAppDatabase

    val context = Any()

    private val tmpFilesToDelete = mutableListOf<File>()

    lateinit var containerTmpDir: File

    lateinit var testFiles: MutableList<File>

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.Companion.getInstance(context)
        repo = db
        db.clearAllTables()

        containerTmpDir = File.createTempFile("testcontainerdir", "tmp")
        containerTmpDir.delete()
        containerTmpDir.mkdir()

        var fCount = 0
        testFiles = mutableListOf()
        testFileNames.forEach {
            val tmpFile = File.createTempFile("testContainerMgr", "testFile${fCount++}")
            extractTestResourceToFile("/com/ustadmobile/core/container/$it",
                    tmpFile)
            testFiles.add(tmpFile)
        }

        testGzipFiles.forEach {
            val tmpFile = File.createTempFile("testContainerMgr", "testFile${fCount++}")
            extractTestResourceToFile("/com/ustadmobile/core/container/$it",
                    tmpFile)
            testFiles.add(tmpFile)
        }

        tmpFilesToDelete.addAll(testFiles)

    }

    @After
    fun cleanup() {
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
                    containerTmpDir.absolutePath)
            manager1.addEntries(ContainerManager.FileEntrySource(testFiles[0], "testfile1.png"))

            val container2 = Container()
            container2.containerUid = repo.containerDao.insert(container2)
            val manager2 = ContainerManager(container2, db, repo,
                    containerTmpDir.absolutePath)

            val filesToAdd2 = HashMap<File, String>()
            filesToAdd2[testFiles[0]] = "testfileothername.png"
            filesToAdd2[testFiles[1]] = "anotherimage.png"
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
            val manager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)

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
            val manager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)

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

    @Test
    @Throws(IOException::class)
    fun givenExistingContainerWithEntries_whenNewContainerManagerIsCreated_thenEntryShouldBeFound() {
        runBlocking {
            val pathList = mutableListOf("path1.txt", "path2.txt", "path3.txt", "path4.txt")
            val entry = ContentEntry()
            entry.title = "Sample Entry Title"
            entry.leaf = true
            entry.contentEntryUid = db.contentEntryDao.insert(entry)
            val container = Container()
            container.containerContentEntryUid = entry.contentEntryUid
            container.containerUid = repo.containerDao.insert(container)

            val containerManager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)

            val fileSources = mutableListOf<ContainerManager.FileEntrySource>()

            pathList.forEach {
                fileSources.add(ContainerManager.FileEntrySource(
                        File.createTempFile("tmp", it), it))
            }
            containerManager.addEntries(*fileSources.toTypedArray())

            Assert.assertTrue("Entries were added to the container", containerManager.allEntries.isNotEmpty())

            val foundContainer = repo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(entry.contentEntryUid)
            Assert.assertNotNull("Container Dao doesn't return empty container", foundContainer)

            val manager = ContainerManager(foundContainer!!, db, repo, containerTmpDir.absolutePath)

            Assert.assertEquals("New container manager should have same number of entries as it was created",
                    containerManager.allEntries.size, manager.allEntries.size)

            val foundEntry = manager.getEntry(pathList.first())

            Assert.assertNotNull("Found entry from constructed container should not be null",
                    foundEntry)


        }
    }

    @Test
    fun givenCompressableEntry_whenAdded_thenShouldBeGzipped() {
        //test that the content is gzipped, but then, when using getInputStream, should be inflated

        val container = Container()
        container.containerUid = db.containerDao.insert(container)

        val containerManager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)
        runBlocking {
            containerManager.addEntries(
                    ContainerManager.FileEntrySource(testFiles[2], "testfile2.png"))

            val containerEntry = containerManager.getEntry("testfile2.png")!!
            Assert.assertEquals("png was gzipped", COMPRESSION_GZIP,
                    containerEntry.containerEntryFile!!.compression)
            Assert.assertTrue("inputstream is GzipInputStream", containerManager.getInputStream(containerEntry) is GZIPInputStream)
            Assert.assertArrayEquals("Byte content should be the same",
                    FileInputStream(testFiles[2]).readBytes(),
                    containerManager.getInputStream(containerEntry).readBytes())

        }


    }

    @Test
    fun givenNonCompressableEntry_whenAdded_thenShouldNotBeGzipped() {
        val container = Container()
        container.containerUid = db.containerDao.insert(container)

        val containerManager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)
        runBlocking {

            containerManager.addEntries(
                    ContainerManager.FileEntrySource(testFiles[3], "BigBuckBunny.mp4"))

            val containerEntry = containerManager.getEntry("BigBuckBunny.mp4")!!
            Assert.assertEquals("mp4 was not gzipped", COMPRESSION_NONE,
                    containerEntry.containerEntryFile!!.compression)
            Assert.assertTrue("inputstream is FileInputStream",containerManager.getInputStream(containerEntry) !is GZIPInputStream)
            Assert.assertArrayEquals("Byte content should be the same",
                    FileInputStream(testFiles[3]).readBytes(),
                    containerManager.getInputStream(containerEntry).readBytes())

        }
    }

    @Test
    fun givenFileExistsInOtherContainer_whenLinkExistingItemsCalled_thenShouldCreateContentEntryAndReturnNonExistingItems() {
        runBlocking {
            val container1 = Container();
            container1.containerUid = db.containerDao.insert(container1)
            val container1Manager = ContainerManager(container1, db, repo, containerTmpDir.absolutePath)
            container1Manager.addEntries(ContainerManager.FileEntrySource(testFiles[0], testFiles[0].name))

            val container2 = Container()
            container2.containerUid = db.containerDao.insert(container2)
            val container2Manager = ContainerManager(container2, db, repo, containerTmpDir.absolutePath)
            val container1Entry = container1Manager.getEntry(testFiles[0].name)!!
            val entryToDownload1= ContainerEntryWithMd5(cefMd5 = container1Entry.containerEntryFile!!.cefMd5)
            entryToDownload1.ceContainerUid = container2.containerUid
            entryToDownload1.cePath = testFiles[0].name
            val entriesToDownload = listOf(entryToDownload1)

            val remainingEntriesToDownload = container2Manager.linkExistingItems(entriesToDownload)

            Assert.assertEquals("After calling linkExistingItems with existing matching md5, entry created",
                    1, container2Manager.allEntries.size)
            Assert.assertEquals("1 Entry for container2 created in db", 1,
                    db.containerEntryDao.findByContainer(container2.containerUid).size)
            Assert.assertTrue("Given 1 already downloaded entry, remaining items to download is empty",
                    remainingEntriesToDownload.isEmpty())
        }
    }

    @Test
    fun givenFileAlreadyInContainer_whenLinkExistingItemsCalled_thenShouldDoNothingAndReturnNonExistingItems() {
        runBlocking {
            val container1 = Container();
            container1.containerUid = db.containerDao.insert(container1)
            val container1Manager = ContainerManager(container1, db, repo, containerTmpDir.absolutePath)
            container1Manager.addEntries(ContainerManager.FileEntrySource(testFiles[0], testFiles[0].name))
            val numEntriesBefore = container1Manager.allEntries.size

            val container1Entry = container1Manager.getEntry(testFiles[0].name)!!
            val entryToDownload1 = ContainerEntryWithMd5(cefMd5 = container1Entry.containerEntryFile!!.cefMd5)
            entryToDownload1.cePath = testFiles[0].name
            entryToDownload1.ceContainerUid = container1.containerUid

            val remainingEntriesToDownload = container1Manager.linkExistingItems(listOf(entryToDownload1))
            Assert.assertEquals("After adding entry, before calling linkExistingItems, size is 1 entry",
                    1, numEntriesBefore)
            Assert.assertEquals("After calling linkExistingItems with existing matching md5, still has one entry",
                    1, container1Manager.allEntries.size)
            Assert.assertEquals("Db has one entry for container", 1,
                    db.containerEntryDao.findByContainer(container1.containerUid).size)

            Assert.assertTrue("Given 1 already downloaded entry, remaining items to download is empty",
                    remainingEntriesToDownload.isEmpty())
        }
    }


}