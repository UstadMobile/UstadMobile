package com.ustadmobile.core.domain

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.assignment.submitassignment.AssignmentSubmissionException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.ext.withDoorTransactionAsync


class SendInvitationUseCase {

    data class SendInvitationResult(
        val result: String?
    )


    @Throws(AssignmentSubmissionException::class)
    suspend operator fun invoke(
        db: UmAppDatabase,
        systemImpl: UstadMobileSystemImpl,
        invitationList: List<String>,
        clazzUid: Long,
    ) : SendInvitationResult {
        return db.withDoorTransactionAsync {

            SendInvitationResult("Ok")
        }
    }

}