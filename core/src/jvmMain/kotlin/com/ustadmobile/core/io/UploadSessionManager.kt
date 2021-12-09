package com.ustadmobile.core.io

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.io.Closeable
import java.io.InputStream
import java.util.*
import java.util.concurrent.locks.ReentrantLock

typealias UploadSessionFactory = (sessionUuid: UUID, containerEntryPaths: List<ContainerEntryWithMd5>,
                                  site: Endpoint, di: DI) -> UploadSession

class UploadSessionManager(
    val site: Endpoint, override val di: DI,
    private val inactiveCheckInterval: Int = 60000,
    private val sessionTimeout: Int = 60000 * 20,
    private val uploadSessionFactory: UploadSessionFactory = DEFAULT_UPLOAD_SESSION_FACTORY
) : Closeable, DIAware{

    private val timeoutChecker = GlobalScope.launch {
        while(true) {

            delay(60000)
        }
    }

    private val activeSessions = mutableMapOf<UUID, UploadSession>()

    private val lock = ReentrantLock()

    fun initSession(sessionUuid: UUID, containerEntryPaths: List<ContainerEntryWithMd5>) : UploadSession {

        return lock.withLock {
            val currentUploadSession = activeSessions.get(sessionUuid)
            if(currentUploadSession != null){
                currentUploadSession.close()
                activeSessions.remove(sessionUuid)
            }

            uploadSessionFactory(sessionUuid, containerEntryPaths, site, di).also {
                activeSessions[sessionUuid] = it
            }
        }
    }

    private fun getSessionOrThrow(sessionUuid: UUID) : UploadSession {
        return lock.withLock {
            activeSessions[sessionUuid]
        } ?: throw IllegalStateException("UploadSessionManager (${site.url}) - no upload session ${sessionUuid}")
    }

    fun onReceiveSessionChunk(sessionUuid: UUID, inputStream: InputStream){
        getSessionOrThrow(sessionUuid).onReceiveChunk(inputStream)
    }

    fun closeSession(sessionUuid: UUID) {
        val session = getSessionOrThrow(sessionUuid)
        session.close()
        lock.withLock {
            activeSessions.remove(sessionUuid)
        }
    }

    override fun close() {
        timeoutChecker.cancel()
    }

    companion object {
        val DEFAULT_UPLOAD_SESSION_FACTORY = {sessionUuid: UUID, containerEntryPaths: List<ContainerEntryWithMd5>,
                                              site: Endpoint, di: DI ->
            UploadSession(sessionUuid.toString(), containerEntryPaths, site.url, di)
        }
    }

}