package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment

class ApproveOrDeclinePendingEnrolmentUseCase(
    private val db: UmAppDatabase
): IApproveOrDeclinePendingEnrolmentRequestUseCase {

    override suspend fun invoke(personUid: Long, clazzUid: Long, approved: Boolean) {
        db.withDoorTransactionAsync {
            val effectiveClazz = db.clazzDao.findByUidAsync(clazzUid)
                ?: throw IllegalStateException("Class does not exist")

            if(approved) {
                //find the group member and update that
                val numGroupUpdates = db.personGroupMemberDao.moveGroupAsync(
                    personUid = personUid,
                    newGroup = effectiveClazz.clazzStudentsPersonGroupUid,
                    oldGroup = effectiveClazz.clazzPendingStudentsPersonGroupUid,
                    changeTime = systemTimeInMillis()
                )

                if(numGroupUpdates != 1) {
                    throw IllegalStateException("Approve pending clazz member - no membership of clazz's pending group!")
                }

                val enrolmentUpdateCount = db.clazzEnrolmentDao.updateClazzEnrolmentRole(
                    personUid = personUid,
                    clazzUid = clazzUid,
                    newRole = ClazzEnrolment.ROLE_STUDENT,
                    oldRole = ClazzEnrolment.ROLE_STUDENT_PENDING,
                    systemTimeInMillis()
                )

                if(enrolmentUpdateCount != 1) {
                    throw IllegalStateException("Approve pending clazz member - no update of enrolment!")
                }

                Unit
            }else {
                db.clazzEnrolmentDao.updateClazzEnrolmentActiveForPersonAndClazz(
                    personUid = personUid,
                    clazzUid = clazzUid,
                    roleId = ClazzEnrolment.ROLE_STUDENT_PENDING,
                    active = false,
                    changeTime = systemTimeInMillis()
                )

                db.personGroupMemberDao.updateGroupMemberActive(
                    activeStatus = false,
                    personUid = personUid,
                    groupUid = effectiveClazz.clazzPendingStudentsPersonGroupUid,
                    updateTime = systemTimeInMillis()
                )
            }
        }
    }
}