package com.ustadmobile.core.domain.clazzenrolment.pendingenrolment

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.EnrolmentRequest

class ApproveOrDeclinePendingEnrolmentUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val enrolIntoCourseUseCase: EnrolIntoCourseUseCase,
): IApproveOrDeclinePendingEnrolmentRequestUseCase {

    override suspend fun invoke(
        enrolmentRequest: EnrolmentRequest,
        approved: Boolean,
    ) {
        val effectiveClazz = db.clazzDao().findByUidAsync(enrolmentRequest.erClazzUid)
            ?: throw IllegalStateException("Class does not exist")

        (repo ?: db).withDoorTransactionAsync {
            val requestStatus = if(approved){
                enrolIntoCourseUseCase(
                    enrolment = ClazzEnrolment(
                        clazzUid = enrolmentRequest.erClazzUid,
                        personUid = enrolmentRequest.erPersonUid,
                        role = ClazzEnrolment.ROLE_STUDENT
                    ),
                    timeZoneId = effectiveClazz.clazzTimeZone ?: "UTC"
                )
                EnrolmentRequest.STATUS_APPROVED
            }else {
                EnrolmentRequest.STATUS_REJECTED
            }

            (repo ?: db).enrolmentRequestDao().updateStatus(
                uid = enrolmentRequest.erUid,
                status = requestStatus,
                updateTime = systemTimeInMillis()
            )
        }
    }
}