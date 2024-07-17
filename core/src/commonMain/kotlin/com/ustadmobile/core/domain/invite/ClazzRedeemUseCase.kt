package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.lib.db.entities.ClazzEnrolment

class ClazzRedeemUseCase(
    private val enrolIntoCourseUseCase: EnrolIntoCourseUseCase,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {
    suspend operator fun invoke(
        inviteCode: String
    ): ClazzRedeemResult {
        val effectiveDb = (repo ?: db)
        val clazzInviteWithTimeZone = effectiveDb.clazzInviteDao().findClazzInviteEntityForInviteToken(inviteCode)
                ?: return ClazzRedeemResult(false, "Invite code is invalid")

        val clazz = clazzInviteWithTimeZone.clazzInvite?.ciUid?.let {

            effectiveDb.clazzEnrolmentDao().findClazzEnrolmentEntityForClazzEnrolmentInviteUid(it)
        }

        if (clazz != null) {
            return ClazzRedeemResult(false, "Invite code is already redeemed")
        } else {
            clazzInviteWithTimeZone.clazzInvite?.let { clazzInvite ->
                enrolIntoCourseUseCase.invoke(
                    enrolment = ClazzEnrolment(
                        clazzUid = clazzInvite.ciClazzUid,
                        personUid = clazzInvite.ciPersonUid,
                        role = clazzInvite.ciRoleId.toInt()
                    ), timeZoneId = clazzInviteWithTimeZone.timeZone ?: "UTC"
                )

                effectiveDb.clazzEnrolmentDao().updateClazzEnrolmentInviteUid(
                    clazzInvite.ciUid,
                    clazzInvite.ciClazzUid
                )

            }

            return ClazzRedeemResult(true, "Invite code redeemed successfully")
        }

    }
}

data class ClazzRedeemResult(
    val isCodeRedeem: Boolean,
    val message: String
)