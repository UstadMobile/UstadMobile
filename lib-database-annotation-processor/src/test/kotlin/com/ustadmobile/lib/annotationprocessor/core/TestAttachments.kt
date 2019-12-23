package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.asRepository
import db2.ExampleAttachmentEntity
import db2.ExampleDatabase2
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class TestAttachments {

    private lateinit var serverDb: ExampleDatabase2

    private lateinit var clientDb: ExampleDatabase2

    private lateinit var serverTmpAttachmentsDir: File

    private lateinit var clientTmpAtttachmentsDir: File

    private lateinit var server: ApplicationEngine

    private lateinit var httpClient: HttpClient

    private lateinit var testTmpDataFile: File

    @Before
    fun setup() {
        serverDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "ExampleDatabase2").build()
        serverDb.clearAllTables()
        clientDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1")
                .build()
        clientDb.clearAllTables()

        testTmpDataFile = File.createTempFile("test", "asset")
        testTmpDataFile .writeBytes(javaClass.getResourceAsStream("/testfile1.png").readBytes())


        serverTmpAttachmentsDir = Files.createTempDirectory("TestAttachmentsServer").toFile()
        clientTmpAtttachmentsDir = Files.createTempDirectory("TestAttachmentsClient").toFile()
        server = embeddedServer(Netty, port = 8089) {
            ExampleDatabase2App(attachmentsDir = serverTmpAttachmentsDir.absolutePath)
        }
        server.start()

        httpClient = HttpClient() {
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        serverTmpAttachmentsDir.deleteRecursively()
        clientTmpAtttachmentsDir.deleteRecursively()
        testTmpDataFile.delete()
        server.stop(0, 20, TimeUnit.SECONDS)
        httpClient.close()
    }

    @Test
    fun givenSetDataCalled_whenGetAttachmentDataCalled_sameDataIsReturned() {
        val dbRepo = serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                "", HttpClient(), serverTmpAttachmentsDir.absolutePath)

        val exampleAttachmentEntity = ExampleAttachmentEntity()
        exampleAttachmentEntity.eaUid = serverDb.exampleAttachmentDao().insert(exampleAttachmentEntity)
        dbRepo.exampleAttachmentDao().setAttachmentData(exampleAttachmentEntity, testTmpDataFile.absolutePath)

        val storedFilePath = dbRepo.exampleAttachmentDao().getAttachmentDataFileName(exampleAttachmentEntity)
        Assert.assertArrayEquals("Content stored is the same as original file", testTmpDataFile.readBytes(),
                File(storedFilePath).readBytes())
    }

    @Test
    fun givenAttachmentDataSetOnServer_whenRepoSelectMethodCalled_thenDataShouldBeAVailableOnClient() {
        val serverRepo = serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                "", httpClient, serverTmpAttachmentsDir.absolutePath)
        val exampleAttachmentEntity = ExampleAttachmentEntity()
        exampleAttachmentEntity.eaUid = serverRepo.exampleAttachmentDao().insert(exampleAttachmentEntity)
        serverRepo.exampleAttachmentDao().setAttachmentData(exampleAttachmentEntity,
                testTmpDataFile.absolutePath)

        val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/",
                "token", httpClient, clientTmpAtttachmentsDir.absolutePath)
                .asConnectedRepository<ExampleDatabase2>()
        val clientEntity = clientRepo.exampleAttachmentDao().findByUid(exampleAttachmentEntity.eaUid)!!
        val clientFilename = clientRepo.exampleAttachmentDao().getAttachmentDataFileName(clientEntity)

        Assert.assertArrayEquals("Data in client attachment is the same as the original file",
                testTmpDataFile.readBytes(), File(clientFilename).readBytes())

    }

    @Test
    fun givenAttachmentOnClient_whenSyncRuns_thenDataShouldBeAvailableOnServer() {
        runBlocking {
            val clientRepo = clientDb.asRepository<ExampleDatabase2>(Any(), "http://localhost:8089/",
                    "token", httpClient, clientTmpAtttachmentsDir.absolutePath)
                    .asConnectedRepository<ExampleDatabase2>()
            val exampleAttachmentEntity = ExampleAttachmentEntity()
            exampleAttachmentEntity.eaUid = clientRepo.exampleAttachmentDao().insert(exampleAttachmentEntity)
            clientRepo.exampleAttachmentDao().setAttachmentData(exampleAttachmentEntity,
                    testTmpDataFile.absolutePath)

            (clientRepo as DoorDatabaseSyncRepository).sync(null)

            val serverRepo = serverDb.asRepository<ExampleDatabase2>(Any(), "http://localhost/dummy",
                    "", httpClient, serverTmpAttachmentsDir.absolutePath)
            val serverEntity = serverDb.exampleAttachmentDao().findByUid(exampleAttachmentEntity.eaUid)
            val serverFilename = serverRepo.exampleAttachmentDao().getAttachmentDataFileName(serverEntity!!)
            Assert.assertTrue(File(serverFilename).exists())
        }


    }


}