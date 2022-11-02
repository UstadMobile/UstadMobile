package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.door.ext.inflatedMd5Sum
import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.*

class UmAppDatabaseContainerIoExtTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    lateinit var tmpFile1: File

    lateinit var tmpFile2: File

    lateinit var tmpDir: File

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    lateinit var containerFilesDir: File

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        tmpDir = temporaryFolder.newFolder()

        tmpFile1 = File(tmpDir, "cat-pic0.jpg")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic0.jpg")
                .writeToFile(tmpFile1)

        tmpFile2 = File(tmpDir, "cat-pic1.jpg")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic1.jpg")
                .writeToFile(tmpFile2)

        containerFilesDir = temporaryFolder.newFolder()
    }

    @Test
    fun givenEmptyContainer_whenAddFileWithDirCalled_thenFilesShouldBeAdded() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val container = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }


        runBlocking {
            repo.addDirToContainer(container.containerUid, tmpDir.toDoorUri(), true, Any(), di,
                ContainerAddOptions(containerFilesDir.toDoorUri()))
        }

        val file1ContainerEntry = db.containerEntryDao.findByPathInContainer(container.containerUid,
            "cat-pic0.jpg")
        val file1InContainer = File(file1ContainerEntry!!.containerEntryFile!!.cefPath)
        Assert.assertTrue("File 1 was copied", file1InContainer.exists())
        Assert.assertArrayEquals("File 1 inflated md5 is the same as original file contents",
            tmpFile1.md5Sum, file1InContainer.inflatedMd5Sum)
    }


    private fun assertZipAndContainerContentsAreEqual(zip: File, db: UmAppDatabase, containerUid: Long) {
        lateinit var entry: ZipEntry
        ZipInputStream(FileInputStream(zip)).use { zipIn ->
            while(zipIn.nextEntry?.also { entry = it } != null) {
                val containerEntryBytes = db.containerEntryDao
                    .openEntryInputStream(containerUid, entry.name)?.readBytes()
                val containerEntryWithFile = db.containerEntryDao.findByPathInContainer(
                    containerUid, entry.name)
                val entryFile = File(containerEntryWithFile!!.containerEntryFile!!.cefPath!!)
                Assert.assertEquals("Compressed size is the size of actual file for ${entry.name}",
                    entryFile.length(), containerEntryWithFile!!.containerEntryFile!!.ceCompressedSize)
                Assert.assertEquals("Uncompressed size matches for ${entry.name}",
                    containerEntryBytes!!.size.toLong(),
                    containerEntryWithFile!!.containerEntryFile!!.ceTotalSize)
                Assert.assertArrayEquals("Entry bytes are the same for ${entry.name}",
                    zipIn.readBytes(), containerEntryBytes)
            }
        }
    }

    @Test
    fun givenEmptyContainer_whenAddFilesFromZipCalled_thenFilesShouldBeAdded() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val container = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }

        val epubTmp = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
                .writeToFile(epubTmp)


        runBlocking {
            repo.addEntriesToContainerFromZip(
                    container.containerUid, epubTmp.toDoorUri(),
                    ContainerAddOptions(containerFilesDir.toDoorUri()), Any()
            )
        }

        assertZipAndContainerContentsAreEqual(epubTmp, db, container.containerUid)
    }

    @Test
    fun givenEmptyContainer_whenAddFilesFromZipContainingDuplicateMd5sCalled_thenShouldBeAdded() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val container = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }

        val zipTmpFile = temporaryFolder.newFile()
        val zipOutStream = ZipOutputStream(FileOutputStream(zipTmpFile))
        zipOutStream.setMethod(ZipEntry.STORED)
        val crc32 = CRC32()

        (0..1).forEach { index ->
            crc32.update(tmpFile1.readBytes())
            zipOutStream.putNextEntry(ZipEntry("file1-$index.jpg").apply {
                size = tmpFile1.length()
                compressedSize = tmpFile1.length()
                crc = crc32.value
            })
            zipOutStream.write(tmpFile1.readBytes())
            crc32.reset()
        }

        crc32.update(tmpFile2.readBytes())
        zipOutStream.putNextEntry(ZipEntry("file.jpg").apply {
            size = tmpFile2.length()
            compressedSize = tmpFile2.length()
            crc = crc32.value
        })
        zipOutStream.write(tmpFile2.readBytes())
        zipOutStream.closeEntry()
        zipOutStream.flush()
        zipOutStream.close()

        runBlocking {
            repo.addEntriesToContainerFromZip(
                container.containerUid, zipTmpFile.toDoorUri(),
                ContainerAddOptions(containerFilesDir.toDoorUri()), Any()
            )
        }

        assertZipAndContainerContentsAreEqual(zipTmpFile, db, container.containerUid)

    }

    //TODO: Test overwriting existing files

    @Test
    fun givenAddContainerCalled_whenAddFileCalled_thenShouldAddFileAndMd5ShouldMatch() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val containerStorageDir = temporaryFolder.newFolder()

        val containerUid = runBlocking {
            repo.containerBuilder(contentEntryUid = 0, mimeType = "application/zip",
                    containerStorageDir.toDoorUri())
                .addFile("cat-pic0.jpg", tmpFile1, ContainerBuilder.Compression.GZIP)
                .build()
        }.containerUid

        val file1ContainerEntry = db.containerEntryDao.findByPathInContainer(containerUid,
            "cat-pic0.jpg")
        val file1InContainer = File(file1ContainerEntry!!.containerEntryFile!!.cefPath)
        Assert.assertArrayEquals("File 1 inflated md5 is the same as original file contents",
            tmpFile1.md5Sum, file1InContainer.inflatedMd5Sum)
    }

    @Test
    fun givenAddContainercalled_whenAddZipCalled_thenAllFilesFromZipShouldBeAdded(){
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val containerStorageDir = temporaryFolder.newFolder()

        val epubTmp = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
            .writeToFile(epubTmp)

        val containerUid = runBlocking {
            repo.containerBuilder(0, "application/zip",
                    containerStorageDir.toDoorUri())
                .addZip(epubTmp)
                .build().containerUid
        }

        assertZipAndContainerContentsAreEqual(epubTmp, db, containerUid)
    }


    @Test
    fun givenAddContainer_whenAddDoorUriCalled_thenShouldAddFile() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val containerStorageDir = temporaryFolder.newFolder()
        val container = runBlocking {
            repo.containerBuilder(0, "application/zip",
                    containerStorageDir.toDoorUri())
                .addUri("catpic0.jpg", tmpFile1.toDoorUri(), Any())
                .build()
        }

        val file1ContainerEntry = db.containerEntryDao.findByPathInContainer(container.containerUid,
            "catpic0.jpg")
        val file1InContainer = File(file1ContainerEntry!!.containerEntryFile!!.cefPath)
        Assert.assertArrayEquals("File 1 inflated md5 is the same as original file contents",
            tmpFile1.md5Sum, file1InContainer.inflatedMd5Sum)

    }

    @Test
    fun givenAddContainer_whenAddTextCalled_thenShouldAddText() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val containerStorageDir = temporaryFolder.newFolder()
        val container = runBlocking {
            repo.containerBuilder(0, "application/zip", containerStorageDir.toDoorUri())
                .addText("hello.txt", "Hello World")
                .build()
        }

        val file1ContainerEntry = db.containerEntryDao.findByPathInContainer(container.containerUid,
            "hello.txt")
        val file1InContainer = File(file1ContainerEntry!!.containerEntryFile!!.cefPath)

        val inflatedText = GZIPInputStream(FileInputStream(file1InContainer)).readString()
        Assert.assertEquals("Hello World", inflatedText)
    }

}