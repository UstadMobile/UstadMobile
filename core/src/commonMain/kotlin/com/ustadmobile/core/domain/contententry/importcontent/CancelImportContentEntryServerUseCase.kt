package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.usersession.ValidateUserSessionOnServerUseCase
import io.github.aakira.napier.Napier

/**
 * Server side implementation to cancel a running import job on request from a client. This will
 * validate that the request comes from a node that has a valid session representing the owner of
 * the import job.
 */
class CancelImportContentEntryServerUseCase(
    private val cancelImportContentEntryUseCase: CancelImportContentEntryUseCase,
    private val validateUserSessionOnServerUseCase: ValidateUserSessionOnServerUseCase,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
) {

    suspend operator fun invoke(
        cjiUid: Long,
        remoteNodeId: Long,
        nodeAuth: String,
        accountPersonUid: Long,
    ) {
        Napier.d { "CancelImportContentEntryServerUseCase: validating session to cancel #$cjiUid"}
        validateUserSessionOnServerUseCase(
            nodeId = remoteNodeId,
            nodeAuth = nodeAuth,
            accountPersonUid = accountPersonUid,
        )

        Napier.d { "CancelImportContentEntryServerUseCase: validating owner to cancel #$cjiUid"}

        val ownerPersonUid = db.contentEntryImportJobDao().findOwnerByUidAsync(cjiUid)
        if(ownerPersonUid != accountPersonUid)
            throw IllegalArgumentException("$accountPersonUid is not owner of the job $cjiUid ($ownerPersonUid)")

        Napier.d { "CancelImportContentEntryServerUseCase: requesting cancellation of #$cjiUid "}

        cancelImportContentEntryUseCase(cjiUid)
        Napier.d { "CancelImportContentEntryServerUseCase: Canceled import #$cjiUid on ${endpoint.url}" }
    }

}