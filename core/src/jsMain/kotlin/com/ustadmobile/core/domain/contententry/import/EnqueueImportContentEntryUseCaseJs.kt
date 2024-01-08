package com.ustadmobile.core.domain.contententry.import

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.ImportRequest
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class EnqueueImportContentEntryUseCaseJs(
    private val endpoint: Endpoint,
    private val httpClient: HttpClient,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(contentJobItem: ContentEntryImportJob) {
        //send request to the server
        httpClient.post("${endpoint.url}api/import/importRequest") {
            contentType(ContentType.Application.Json)
            setBody(
                ImportRequest(
                    contentJobItem = contentJobItem,
                )
            )
        }
    }
}