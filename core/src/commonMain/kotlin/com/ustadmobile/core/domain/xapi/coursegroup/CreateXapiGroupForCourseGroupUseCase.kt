package com.ustadmobile.core.domain.xapi.coursegroup

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiGroup
import com.ustadmobile.core.domain.xapi.model.identifierHash
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.util.ext.toXapiAgent

/**
 * Create a Xapi Identified group that represents the a particular group number for a particular
 * CourseGroupSet
 */
class CreateXapiGroupForCourseGroupUseCase(
    private val repo: UmAppDatabase,
    private val learningSpace: LearningSpace,
    private val stringHasher: XXStringHasher,
)  {

    data class XapiGroupAndPersonUidMap(
        val group: XapiGroup,
        val actorUidToPersonUidMap: Map<Long, Long>
    )

    suspend operator fun invoke(
        groupSetUid: Long,
        groupNum: Int,
        clazzUid: Long,
        assignmentUid: Long,
        accountPersonUid: Long,
    ): XapiGroupAndPersonUidMap {
        val groupMembers = repo.courseGroupMemberDao()
            .findByCourseGroupSetAndGroupNumAsync(
                courseGroupSetUid = groupSetUid,
                groupNum = groupNum,
                clazzUid = clazzUid,
                assignmentUid = assignmentUid,
                accountPersonUid = accountPersonUid
            )

        val membersAndPersonUids = groupMembers.mapNotNull { courseGroupMember ->
            courseGroupMember.person?.toXapiAgent(learningSpace)?.let {
                Pair(it, courseGroupMember.courseGroupMember?.cgmPersonUid ?: 0)
            }
        }

        return XapiGroupAndPersonUidMap(
            group = XapiGroup(
                account = XapiAccount(
                    homePage = learningSpace.url,
                    name = "cgs-$groupSetUid-$groupNum"
                ),
                member = membersAndPersonUids.map { it.first }
            ),
            actorUidToPersonUidMap = membersAndPersonUids.associate {
                it.first.identifierHash(stringHasher) to it.second
            }
        )
    }

}