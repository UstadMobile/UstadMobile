package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * Dismiss a ContentEntryImportJob error that occurred on the server.
 */
class DismissRemoteContentEntryImportErrorUseCase(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
    private val repo: UmAppDatabase,
) {

    suspend operator fun invoke(
        cjiUid: Long,
        activeUserPersonUid: Long,
    ) {
        val repoVal = repo as? DoorDatabaseRepository
            ?: throw IllegalArgumentException("no repo")

        httpClient.get("${learningSpace.url}api/contententryimportjob/dismissError") {
            parameter("jobUid", cjiUid)
            doorNodeIdHeader(repoVal)
            parameter("accountPersonUid", activeUserPersonUid)
            header("cache-control", "no-store")
        }
    }

}