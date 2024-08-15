package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.door.ext.setBodyJson
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class EnqueueImportContentEntryUseCaseRemote(
    private val learningSpace: LearningSpace,
    private val httpClient: HttpClient,
    private val json: Json,
) : EnqueueContentEntryImportUseCase {

    override suspend fun invoke(contentJobItem: ContentEntryImportJob) {
        //send request to the server
        httpClient.post("${learningSpace.url}api/import/importRequest") {
            contentType(ContentType.Application.Json)
            setBodyJson(
                json = json,
                serializer = ImportRequest.serializer(),
                value = ImportRequest(
                    contentJobItem = contentJobItem,
                )
            )
        }
    }
}