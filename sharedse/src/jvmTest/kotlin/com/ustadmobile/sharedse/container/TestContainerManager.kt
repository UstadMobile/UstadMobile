package com.ustadmobile.sharedse.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class TestContainerManager  {

    val testFileNames = listOf("testfile1.png", "testfile2.png")

    lateinit var db: UmAppDatabase

    lateinit var repo: UmAppDatabase

    val context = Any()

    @Before
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.Companion.getInstance(context)
        repo = db
        db.clearAllTables()
    }

    @Test
    fun `Given file entry added WHEN getInputStream called THEN should return input`() {
        var fCount = 0
        val testFiles = mutableListOf<File>()
        testFileNames.forEach {
            val tmpFile = File.createTempFile("testContainerMgr", "testFile${fCount++}")
            extractTestResourceToFile("/com/ustadmobile/port/sharedse/container/$it",
                    tmpFile)
            testFiles.add(tmpFile)
        }

        val container = Container()
        container.containerUid = db.containerDao.insert(container)

        val tmpDir = File.createTempFile("testcontainerdir", "tmp")
        tmpDir.delete()
        tmpDir.mkdir()

        val containerManager = ContainerManager(container, db, repo, tmpDir.absolutePath)
        runBlocking {
            containerManager.addEntries(listOf(ContainerManager.FileEntrySource(testFiles[0], "testfile1.png")))

            val containerInStream = containerManager.getInputStream(containerManager.getEntry("testfile1.png")!!)
            containerInStream.use {
                Assert.assertArrayEquals("Stream provided by container is the same as provided by file",
                        testFiles[0].readBytes(), UMIOUtils.readStreamToByteArray(containerInStream))
            }
        }


    }

}