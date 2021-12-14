package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.network.NetworkProgressListener
import com.ustadmobile.core.network.containeruploader.ContainerUploader2
import com.ustadmobile.core.network.containeruploader.ContainerUploaderRequest2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Default implementation of uploading a container to the server
 */
class DefaultContentPluginUploader(
    override val di: DI
): ContentPluginUploader, DIAware {

    override suspend fun upload(
        contentJobItem: ContentJobItem,
        progress: NetworkProgressListener?,
        httpClient: HttpClient,
        endpoint: Endpoint
    ) {
        withContext(Dispatchers.Default) {
            try {
                val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

                val containerEntries = db.containerEntryDao.findByContainer(
                    contentJobItem.cjiContainerUid)

                var uploadSessionUuid = contentJobItem.cjiUploadSessionUid
                if(uploadSessionUuid == null) {
                    uploadSessionUuid = randomUuid().toString()
                    contentJobItem.cjiUploadSessionUid = uploadSessionUuid
                    db.contentJobItemDao.updateUploadSessionUuid(contentJobItem.cjiUid,
                        uploadSessionUuid)
                }

                val uploadRequest = ContainerUploaderRequest2(uploadSessionUuid,
                    containerEntries.map { it.toContainerEntryWithMd5() }, endpoint.url)
                ContainerUploader2(uploadRequest, endpoint = endpoint, di = di,
                    progressListener = progress).upload()
            } catch (c: CancellationException) {
                withContext(NonCancellable){
                    // TODO delete progress if cancelled
                }
                throw c
            }
        }
    }
}