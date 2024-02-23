package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toLocalEndOfDay
import com.ustadmobile.core.util.ext.toLocalMidnight
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonGroupMember
import kotlinx.datetime.Instant

/**
 * Handle enrolling a person into a course. This will insert the Enrolment entity and, if required,
 * the PersonGroupMember required for them to get the permissions they should receive
 */
class EnrolIntoCourseUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) {

    /**
     * Process the given enrolment.
     *
     * Important: This should **NOT** be called from within a transaction. It may use the repository
     * to make http calls.
     */
    suspend operator fun invoke(
        enrolment: ClazzEnrolment,
        timeZoneId: String,
    ) {
        enrolment.clazzEnrolmentDateJoined = Instant
            .fromEpochMilliseconds(enrolment.clazzEnrolmentDateJoined)
            .toLocalMidnight(timeZoneId).toEpochMilliseconds()

        if(enrolment.clazzEnrolmentDateLeft != Long.MAX_VALUE){
            enrolment.clazzEnrolmentDateLeft = Instant
                .fromEpochMilliseconds(enrolment.clazzEnrolmentDateLeft)
                .toLocalEndOfDay(timeZoneId).toEpochMilliseconds()
        }

        val clazzVal = db.clazzDao.findByUidAsync(enrolment.clazzEnrolmentClazzUid)
            ?: repo?.clazzDao?.findByUidAsync(enrolment.clazzEnrolmentClazzUid)
            ?: throw IllegalStateException("Cannot find Clazz for enrolment: ${enrolment.clazzEnrolmentClazzUid}")

        val personGroupUid = when(enrolment.clazzEnrolmentRole) {
            ClazzEnrolment.ROLE_TEACHER -> clazzVal.clazzTeachersPersonGroupUid
            ClazzEnrolment.ROLE_STUDENT -> clazzVal.clazzStudentsPersonGroupUid
            ClazzEnrolment.ROLE_PARENT -> clazzVal.clazzParentsPersonGroupUid
            ClazzEnrolment.ROLE_STUDENT_PENDING -> clazzVal.clazzPendingStudentsPersonGroupUid
            else -> -1
        }

        val existingGroupMemberships =
            repo?.personGroupMemberDao?.checkPersonBelongsToGroup(
                personGroupUid, enrolment.clazzEnrolmentPersonUid
            )
            ?: db.personGroupMemberDao.checkPersonBelongsToGroup(
                personGroupUid, enrolment.clazzEnrolmentPersonUid
            )

        db.withDoorTransactionAsync {
            (repo ?: db).clazzEnrolmentDao.insertAsync(enrolment)
            (repo ?: db).personGroupMemberDao.takeIf { existingGroupMemberships.isEmpty() }
                ?.insertAsync(PersonGroupMember().apply {
                    groupMemberPersonUid = enrolment.clazzEnrolmentPersonUid
                    groupMemberGroupUid = personGroupUid
                }
            )
        }
    }

}