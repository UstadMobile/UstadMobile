package com.ustadmobile.core.io

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import java.io.ByteArrayInputStream
import java.util.*

class UploadSessionManagerTest {

    lateinit var mockUploadSessions: MutableList<UploadSession>

    lateinit var mockUploadSessionFactory: UploadSessionFactory

    @Before
    fun setup() {
        mockUploadSessions = mutableListOf()


        mockUploadSessionFactory = { sessionUuid: UUID, containerEntryPaths: List<ContainerEntryWithMd5>,
                                     site: Endpoint, di: DI ->
            val session: UploadSession = mock {
                on { startFromByte }.thenReturn(42L)
            }

            mockUploadSessions.add(session)

            session
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
        sessionManager.initSession(sessionUuid, containerEntries)
        sessionManager.onReceiveSessionChunk(sessionUuid, ByteArrayInputStream(ByteArray(200)))
        sessionManager.closeSession(sessionUuid)

        verify(mockUploadSessions[0]).onReceiveChunk(any())
        verify(mockUploadSessions[0]).close()
    }


    @Test
    fun givenSessionAlreadyInitialized_whenCreateSessionCalledAgain_thenShouldCloseOldSession() {
        val di = DI {

        }

        val site = Endpoint("http://localhost/")
        val sessionManager = UploadSessionManager(site, di, uploadSessionFactory = mockUploadSessionFactory)
        val containerEntries = listOf(ContainerEntryWithMd5().apply {
            cefMd5 = "aabbcc"
        })

        val sessionUuid = UUID.randomUUID()
        sessionManager.initSession(sessionUuid, containerEntries)
        sessionManager.initSession(sessionUuid, containerEntries)

        verify(mockUploadSessions[0]).close()
    }


}