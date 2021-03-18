package com.ustadmobile.core.io

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import java.io.ByteArrayInputStream
import java.util.*

class UploadSessionManagerTest {

    lateinit var mockUploadSession: UploadSession

    lateinit var mockUploadSessionFactory: UploadSessionFactory

    @Before
    fun setup() {
        mockUploadSession = mock {
            on { startFromByte }.thenReturn(42L)
        }

        mockUploadSessionFactory = { sessionUuid: UUID, containerEntryPaths: List<ContainerEntryWithMd5>,
                                     md5sExpected: List<String>, site: Endpoint, di: DI ->
            mockUploadSession
        }
    }

    @Test
    fun givenSessionInitialized_whenOnReceivedChunkAndThenCloseCalled_thenShouldInvoke() {
        val di = DI {

        }

        val site = Endpoint("http://localhost/")
        val sessionManager = UploadSessionManager(site, di, uploadSessionFactory = mockUploadSessionFactory)
        val containerEntries = listOf(ContainerEntryWithMd5().apply {
            cefMd5 = "aabbcc"
        })

        val sessionUuid = UUID.randomUUID()
        sessionManager.initSession(sessionUuid, containerEntries, listOf("aabbcc"))
        sessionManager.onReceiveSessionChunk(sessionUuid, ByteArrayInputStream(ByteArray(200)))
        sessionManager.closeSession(sessionUuid)

        verify(mockUploadSession).onReceiveChunk(any())
        verify(mockUploadSession).close()
    }


    @Test(expected = IllegalStateException::class)
    fun givenSessionAlreadyInitialized_whenCreateSessionCalledAgain_thenShouldThrowException() {
        val di = DI {

        }

        val site = Endpoint("http://localhost/")
        val sessionManager = UploadSessionManager(site, di, uploadSessionFactory = mockUploadSessionFactory)
        val containerEntries = listOf(ContainerEntryWithMd5().apply {
            cefMd5 = "aabbcc"
        })

        val sessionUuid = UUID.randomUUID()
        sessionManager.initSession(sessionUuid, containerEntries, listOf("aabbcc"))
        sessionManager.initSession(sessionUuid, containerEntries, listOf("aabbcc"))
    }


}