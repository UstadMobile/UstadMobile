package com.ustadmobile.core.domain.person.bulkadd

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.account.VerifyClientUserSessionUseCase

/**
 *
 */
class EnqueueBulkAddPersonServerUseCase(
    private val verifyClientSessionUseCase: VerifyClientUserSessionUseCase,
    private val enqueueBulkAddPersonUseCase: EnqueueBulkAddPersonUseCase,
    private val activeDb: UmAppDatabase,
) {

    suspend operator fun invoke(
        accountPersonUid: Long,
        fromNodeId: Long,
        nodeAuth: String,
        csvData: String,
    ) : Long {
        verifyClientSessionUseCase(
            fromNodeId = fromNodeId,
            nodeAuth = nodeAuth,
            accountPersonUid = accountPersonUid,
        )

        if(!activeDb.systemPermissionDao().personHasSystemPermission(
                accountPersonUid,
                PermissionFlags.ADD_PERSON
        )) {
            throw IllegalStateException("Person $accountPersonUid does not have add permission")
        }

        return enqueueBulkAddPersonUseCase(csvData)
    }

}