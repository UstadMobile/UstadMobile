package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.door.ext.toHexString
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

class ContainerEntryFileDaoExtTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    lateinit var epubTmpFile: File

    lateinit var tmpDir: File

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        tmpDir = temporaryFolder.newFolder()

        epubTmpFile = File(tmpDir, "test.epub")

        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
                .writeToFile(epubTmpFile)

    }

    @Test
    fun givenContainerWithEntries_whenGenerateConcatenatedResponseCalled_thenShouldWriteData() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()

        val container = Container().apply {
            this.containerUid = repo.containerDao.insert(this)
        }

        val storageDir = temporaryFolder.newFolder()

        runBlocking {
            repo.addEntriesToContainerFromZip(container.containerUid, epubTmpFile.toKmpUriString(),
                    ContainerAddOptions(storageDir.toKmpUriString(), false))
        }

        //get a list of all the md5s
        val containerEntryFiles = db.containerEntryDao.findByContainer(container.containerUid)
        val requestMd5s = containerEntryFiles.mapNotNull {
            it.containerEntryFile?.cefMd5?.base64StringToByteArray()?.toHexString()
        }.sorted().joinToString(separator = ";")


        val concatResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse2(
                requestMd5s, mapOf(), db)

        val tmpFile = temporaryFolder.newFile()
        FileOutputStream(tmpFile).use {
            concatResponse.writeTo(it)

        }

        //read the response
        var numEntries = 0

        ConcatenatedInputStream2(FileInputStream(tmpFile)).use {concatIn ->
            while(concatIn.getNextEntry() != null) {
                numEntries++
            }
        }

        Assert.assertEquals("Number of entries in response matches number in zip",
                containerEntryFiles.size, numEntries)

        //Actual content of the files is already checked by the md5sum of ConcatenatedInputStream2
    }

}