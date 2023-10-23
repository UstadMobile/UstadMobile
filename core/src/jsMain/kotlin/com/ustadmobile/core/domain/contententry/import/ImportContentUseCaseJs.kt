package com.ustadmobile.core.domain.contententry.import

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ImportContentUseCaseJs(
    private val endpoint: Endpoint,
    private val httpClient: HttpClient,
) : ImportContentUseCase{

    override suspend fun invoke(contentJob: ContentJob, contentJobItem: ContentJobItem) {
        //send request to the server
        httpClient.post("${endpoint.url}api/import/importRequest") {
            contentType(ContentType.Application.Json)
            setBody(
                ImportRequest(
                    contentJob = contentJob,
                    contentJobItem = contentJobItem,
                )
            )
        }
    }
}