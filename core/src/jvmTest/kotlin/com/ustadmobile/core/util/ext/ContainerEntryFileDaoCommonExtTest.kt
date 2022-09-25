package com.ustadmobile.core.util.ext


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert
import java.io.File
import org.junit.rules.TemporaryFolder

class ContainerEntryFileDaoCommonExtTest {

    lateinit var db: UmAppDatabase

    lateinit var tmpFile1: File

    lateinit var tmpFile2: File

    lateinit var containerEntryFile1: ContainerEntryFile

    lateinit var containerEntryFile2: ContainerEntryFile

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .build()
        db.clearAllTables()

        tmpFile1 = temporaryFolder.newFile()
        tmpFile2 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/testfile1.png")
            .writeToFile(tmpFile1)
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/testfile2.png")
            .writeToFile(tmpFile2)

        containerEntryFile1 = ContainerEntryFile().apply {
            cefMd5 = tmpFile1.md5Sum.encodeBase64()
            cefPath = tmpFile1.absolutePath
            cefUid = db.containerEntryFileDao.insert(this)
        }

        containerEntryFile2 = ContainerEntryFile().apply {
            cefMd5 = tmpFile2.md5Sum.encodeBase64()
            cefPath = tmpFile2.absolutePath
            cefUid = db.containerEntryFileDao.insert(this)
        }
    }

    @Test
    fun givenMultipleContainerEntries_whenDeleteZombiesCalled_thenZombiesShouldBeDeletedOthersShouldRemain() {
        db.containerEntryDao.insertList(listOf(
            ContainerEntry().apply {
                ceContainerUid = 1
                cePath = "file1.png"
                ceCefUid = containerEntryFile1.cefUid
            }
        ))

        runBlocking {
            db.withDoorTransactionAsync { txDb ->
                txDb.containerEntryFileDao.deleteZombieContainerEntryFiles(db.dbType())
            }
        }

        Assert.assertNull("File2 is a zombie, and is no longer found in db after deletion",
            db.containerEntryFileDao.findByUid(containerEntryFile2.cefUid))
        Assert.assertFalse("File2 does not exist anymore",
            File(containerEntryFile2.cefPath!!).exists())

        Assert.assertNotNull("File1 is not a zombie, and is found in db after deleting zombies",
            db.containerEntryFileDao.findByUid(containerEntryFile1.cefUid))
        Assert.assertTrue("File1 still exists on disk",
            File(containerEntryFile1.cefPath!!).exists())
    }

}