package com.ustadmobile.core.util

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.util.commontest.ext.mockResponseForConcatenatedFiles2Request
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import org.kodein.di.DI
import java.util.concurrent.atomic.AtomicInteger

class ConcatenatedResponse2Dispatcher(
    private val db: UmAppDatabase,
    val di: DI,
    val containerUid: Long
) : Dispatcher() {

    var numTimesToFail = AtomicInteger(0)

    override fun dispatch(request: RecordedRequest): MockResponse {
        return if (request.requestUrl?.toUri().toString()
                .contains("ContainerEntryList/findByContainerWithMd5")) {

            val list = db.containerEntryDao.findByContainer(containerUid)
                .map { it.toContainerEntryWithMd5() }
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Buffer().write(safeStringify(di, ListSerializer(ContainerEntryWithMd5.serializer()), list).toByteArray()))

        } else {
            db.mockResponseForConcatenatedFiles2Request(request).apply {
                if (numTimesToFail.getAndDecrement() > 0) {
                    socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
                }
            }
        }
    }
}