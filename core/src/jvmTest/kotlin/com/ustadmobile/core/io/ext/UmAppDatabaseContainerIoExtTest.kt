package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
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
    }

    //TODO: Test overwriting existing files


}