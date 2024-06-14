package com.ustadmobile.core.domain.xapi.coursegroup

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiGroup
import com.ustadmobile.core.util.ext.toXapiAgent

/**
 * Create a Xapi Identified group that represents the a particular group number for a particular
 * CourseGroupSet
 */
class CreateXapiGroupForCourseGroupUseCase(
    private val repo: UmAppDatabase,
    private val endpoint: Endpoint,
)  {

    suspend operator fun invoke(
        groupSetUid: Long,
        groupNum: Int,
        clazzUid: Long,
        assignmentUid: Long,
        accountPersonUid: Long,
    ): XapiGroup {
        val groupMembers = repo.courseGroupMemberDao
            .findByCourseGroupSetAndGroupNumAsync(
                courseGroupSetUid = groupSetUid,
                groupNum = groupNum,
                clazzUid = clazzUid,
                assignmentUid = assignmentUid,
                accountPersonUid = accountPersonUid
            )

        return XapiGroup(
            account = XapiAccount(
                homePage = endpoint.url,
                name = "cgs-$groupSetUid-$groupNum"
            ),
            member = groupMembers.mapNotNull {
                it.person?.toXapiAgent(endpoint)
            }
        )
    }

}