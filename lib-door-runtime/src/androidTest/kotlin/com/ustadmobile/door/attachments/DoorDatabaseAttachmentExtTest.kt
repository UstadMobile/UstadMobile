package com.ustadmobile.door.attachments

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DummyEntityWithAttachment
import com.ustadmobile.door.ext.hexStringToByteArray
import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.writeToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class DoorDatabaseAttachmentExtTest {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    lateinit var tempAttachmentFile: File

    lateinit var shadowContentResolver: ShadowContentResolver

    lateinit var attachmentPath: String

    lateinit var repo: DoorDatabaseRepository


    @Before
    fun setup() {
        tempAttachmentFile = temporaryFolder.newFile()
        attachmentPath = tempAttachmentFile.absolutePath


        this::class.java.getResourceAsStream("/test-resources/cat-pic0.jpg")!!
                .writeToFile(tempAttachmentFile)

        val androidContext = ApplicationProvider.getApplicationContext<Context>()

        shadowContentResolver = shadowOf(androidContext.contentResolver)
        shadowContentResolver.registerInputStream(Uri.fromFile(tempAttachmentFile),
                FileInputStream(tempAttachmentFile))

        val tmpFolder = temporaryFolder.newFolder()
        repo = mock {
            on { attachmentsDir }.thenReturn(tmpFolder.absolutePath)
            on { context }.thenReturn(androidContext)
        }
    }

    @Test
    fun givenValidUri_whenStored_thenCanBeRetrievedAndMd5IsSet() {
        val dummyEntity = DummyEntityWithAttachment().apply {
            attachmentUri = Uri.fromFile(tempAttachmentFile).toString()
        }

        runBlocking {
            repo.storeAttachment(dummyEntity)
        }

        Assert.assertArrayEquals("Md5 sum assigned matches", File(attachmentPath).md5Sum,
                dummyEntity.attachmentMd5?.hexStringToByteArray())

        runBlocking {
            val attachmentRetrievedUri = repo.retrieveAttachment(dummyEntity.attachmentUri!!)
            val storedUri = Uri.parse(attachmentRetrievedUri)
            val storedFile = File(storedUri.path!!)
            Assert.assertArrayEquals("Data stored is the same as data provided",
                    File(attachmentPath).md5Sum, storedFile.md5Sum)
        }
    }


}