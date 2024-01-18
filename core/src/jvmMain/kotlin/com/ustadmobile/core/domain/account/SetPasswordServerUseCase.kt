package com.ustadmobile.core.domain.account

import com.ustadmobile.core.db.UmAppDatabase

/**
 * This use case handles requests from Javascript clients to change passwords.
 */
class SetPasswordServerUseCase(
    private val db: UmAppDatabase,
    private val setPasswordUseCase: SetPasswordUseCase,
) {

    suspend operator fun invoke(
        fromNodeId: Long,
        nodeAuth: String,
        nodeActiveUserUid: Long,
        personUid: Long,
        username: String,
        currentPassword: String?,
        newPassword: String
    ) {
        //Validate from node credentials and run permission check

        setPasswordUseCase(
            personUid = personUid,
            username = username,
            newPassword = newPassword,
            currentPassword = currentPassword,
        )
    }

}