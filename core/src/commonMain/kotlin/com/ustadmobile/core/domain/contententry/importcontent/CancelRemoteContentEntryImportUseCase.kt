package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * Cancels a content entry import where the import is running on the server
 */
class CancelRemoteContentEntryImportUseCase(
    private val endpoint: Endpoint,
    private val httpClient: HttpClient,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke(
        cjiUid: Long,
        activeUserPersonUid: Long,
    ) {
        val repoVal = repo as? DoorDatabaseRepository
            ?: throw IllegalArgumentException()

        httpClient.get("${endpoint.url}api/contententryimportjob/cancel") {
            parameter("jobUid", cjiUid)
            doorNodeIdHeader(repoVal)
            parameter("accountPersonUid", activeUserPersonUid)
            header("cache-control", "no-store")
        }
    }

}