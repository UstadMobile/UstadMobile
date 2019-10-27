package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.asRepository
import db2.ExampleAttachmentEntity
import db2.ExampleDatabase2
import io.ktor.client.HttpClient
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class TestAttachments {

    lateinit var exampleDb2: ExampleDatabase2

    var tmpAttachmentsDir: File? = null

    @Before
    fun setup() {
        exampleDb2 = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        exampleDb2.clearAllTables()
        tmpAttachmentsDir = Files.createTempDirectory("TestAttachments").toFile()
    }

    @After
    fun tearDown() {
        tmpAttachmentsDir?.deleteRecursively()
        tmpAttachmentsDir = null
    }

    @Test
    fun givenSetDataCalled_whenGetAttachmentDataCalled_sameDataIsReturned() {
        val dbRepo = exampleDb2.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                "", HttpClient(), tmpAttachmentsDir!!.absolutePath)
        val tmpFile = File.createTempFile("test", "asset")

        val fileContent = javaClass.getResourceAsStream("/testfile1.png").readBytes()
        tmpFile.writeBytes(fileContent)
        val exampleAttachmentEntity = ExampleAttachmentEntity()
        exampleAttachmentEntity.eaUid = exampleDb2.exampleAttachmentDao().insert(exampleAttachmentEntity)

        dbRepo.exampleAttachmentDao().setAttachmentData(exampleAttachmentEntity, tmpFile.absolutePath)

        val storedFilePath = dbRepo.exampleAttachmentDao().getAttachmentDataFileName(exampleAttachmentEntity)
        Assert.assertArrayEquals("Content stored is the same as original file", fileContent,
                File(storedFilePath).readBytes())
    }

}