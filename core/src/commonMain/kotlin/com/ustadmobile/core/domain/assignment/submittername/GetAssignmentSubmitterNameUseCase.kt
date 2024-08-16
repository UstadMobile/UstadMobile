package com.ustadmobile.core.domain.assignment.submittername

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

class GetAssignmentSubmitterNameUseCase(
    private val repo: UmAppDatabase,
    private val systemImpl: UstadMobileSystemImpl,
) {

    suspend operator fun invoke(submitterUid: Long) : String {
        return if(submitterUid < CourseAssignmentSubmission.MIN_SUBMITTER_UID_FOR_PERSON) {
            systemImpl.getString(MR.strings.group) + " " + submitterUid
        }else {
            repo.personDao().getNamesByUid(submitterUid).filter {
                it?.firstNames != null
            }.first().let {  "${it?.firstNames} ${it?.lastName}" }
        }
    }

}