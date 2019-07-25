package com.ustadmobile.core.util

import com.ustadmobile.core.CompressJob
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class CompressJobTest {

    private val testGzipFiles = listOf("testfile2.png", "BigBuckBunny.mp4")

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var container: Container

    private val tmpFilesToDelete = mutableListOf<File>()

    lateinit var containerTmpDir: File

    lateinit var testFiles: MutableList<File>

    @Before
    fun setup() {
        checkJndiSetup()

        context = Any()
        try {
            db = UmAppDatabase.getInstance(context)
            repo = db//.getRepository("http://localhost/dummy/", "")
            db.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        containerTmpDir = File.createTempFile("testcontainerdir", "tmp")
        containerTmpDir.delete()
        containerTmpDir.mkdir()

        container = Container()
        container.containerUid = repo.containerDao.insert(container)


        var fCount = 0
        testFiles = mutableListOf()
        testGzipFiles.forEach {
            val tmpFile = File.createTempFile("testContainerMgr", "testFile${fCount++}")
            extractTestResourceToFile("/com/ustadmobile/core/container/$it",
                    tmpFile)
            testFiles.add(tmpFile)
        }

        tmpFilesToDelete.addAll(testFiles)


        val containerManager = ContainerManager(container, db, repo, containerTmpDir.absolutePath)

        runBlocking {
            containerManager.addEntries(
                    ContainerManager.FileEntrySource(testFiles[0], "testfile2.png"))
            containerManager.addEntries(
                    ContainerManager.FileEntrySource(testFiles[1], "BigBuckBunny.mp4"))
        }

    }

    @After
    fun cleanup() {
        tmpFilesToDelete.forEach { it.delete() }
        tmpFilesToDelete.clear()
        containerTmpDir.deleteRecursively()
    }

    @Test
    fun test(){
        CompressJob()
    }

}