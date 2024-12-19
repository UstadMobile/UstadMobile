package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorNodeAndVersionHeaders
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import js.promise.await
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import web.http.fetch
import io.ktor.http.content.TextContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class BulkAddPersonsFromLocalUriUseCaseJs(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
    private val json: Json,
    private val repo: UmAppDatabase,
): BulkAddPersonsFromLocalUriUseCase {

    override suspend fun invoke(
        uri: DoorUri,
        accountPersonUid: Long,
        onProgress: BulkAddPersonsUseCase.BulkAddOnProgress
    ): BulkAddPersonsUseCase.BulkAddUsersResult {
        console.log("Getting csv text")
        val text = fetch(uri.toString()).text().await()
        console.log("csv text = $text")
        return coroutineScope {
            val jobId = httpClient.post("${learningSpace.url}api/person/bulkadd/enqueue") {
                setBody(TextContent(text, ContentType.Text.CSV))
                parameter("accountPersonUid", accountPersonUid.toString())
                doorNodeAndVersionHeaders(repo as DoorDatabaseRepository)
            }.bodyAsText().toLong()

            while(isActive) {
                try {
                    val statusResponse = httpClient.get("${learningSpace.url}api/person/bulkadd/status") {
                        parameter("timestamp", jobId.toString())
                    }

                    if(statusResponse.status.value == 200) {
                        val statusJson = json.decodeFromString<BulkAddPersonRunImportUiState>(
                            statusResponse.bodyAsText()
                        )

                        if(statusJson.inProgress) {
                            onProgress(statusJson.numImported, statusJson.totalRecords)
                        }else {
                            if(statusJson.hasErrors) {
                                throw BulkAddPersonException(
                                    message = statusJson.errorMessage,
                                    errors = statusJson.errors,
                                )
                            }

                            return@coroutineScope BulkAddPersonsUseCase.BulkAddUsersResult(
                                numImported = statusJson.numImported
                            )
                        }
                    }
                }catch(e: Throwable) {
                    Napier.w("BulkAddPersonFromLocalUriUseCaseJs exception", e)

                    if(e is BulkAddPersonException)
                        throw e
                }

                delay(1_000)
            }

            throw IllegalStateException("Should not get here")
        }
    }
}