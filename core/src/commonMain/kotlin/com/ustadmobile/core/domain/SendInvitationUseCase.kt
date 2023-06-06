package com.ustadmobile.core.domain

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.assignment.submitassignment.AssignmentSubmissionException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.ext.withDoorTransactionAsync


/**
 * Peer assignment submission requires an allocation of the who marks who.
 */
class SendInvitationUseCase {

    data class SendInvitationResult(
        val result: String?
    )

    /**
     * @param db the system database to save in
     * @param systemImpl
     * @param assignmentUid assignment uid that is being submitted
     * @param accountPersonUid the active user who is submitting
     * @param submission the CourseAssignment the user wants to submit
     */
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