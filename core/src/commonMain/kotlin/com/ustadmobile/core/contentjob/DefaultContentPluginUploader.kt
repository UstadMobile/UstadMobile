package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.network.NetworkProgressListener
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.DIAware

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
        endpoint: Endpoint,
    ): Int {
        TODO()
    }
}