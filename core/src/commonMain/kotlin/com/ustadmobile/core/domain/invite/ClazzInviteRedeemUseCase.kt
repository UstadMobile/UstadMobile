package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.core.MR
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis

data class ClazzRedeemResult(
    val message: String
)

class ClazzInviteRedeemUseCase(
    private val enrolIntoCourseUseCase: EnrolIntoCourseUseCase,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
    private val systemImpl: UstadMobileSystemImpl,
) {

    suspend operator fun invoke(
        inviteCode: String,
        isAccepting: Boolean,
        personUid: Long
    ): ClazzRedeemResult {
        val effectiveDb = (repo ?: db)
        val clazzInviteWithTimeZone = effectiveDb.clazzInviteDao()
            .findClazzInviteEntityForInviteToken(inviteCode)
        val clazzInvite = clazzInviteWithTimeZone?.clazzInvite ?:
            throw ClazzInviteRedeemException("Invite not found", MR.strings.invalid_invite_code)

        if(clazzInvite.inviteStatus != ClazzInvite.STATUS_PENDING) {
            throw ClazzInviteRedeemException("Invite already used", MR.strings.invite_has_been_used)
        }
        if (clazzInvite.inviteExpire < systemTimeInMillis()){
            return ClazzInviteRedeemException("Invite code is expired",MR.strings.invite_code_expired))
        }
         if(clazzInvite.inviteStatus==ClazzInvite.STATUS_REVOKED){
            return ClazzInviteRedeemException(systemImpl.getString(MR.strings.invitation_is_revoked))
        }
        if (isAccepting) {
            val enrolmentUid = enrolIntoCourseUseCase(
                enrolment = ClazzEnrolment(
                    clazzUid = clazzInvite.ciClazzUid,
                    personUid = personUid,
                    role = clazzInvite.ciRoleId.toInt()
                ),
                timeZoneId = clazzInviteWithTimeZone.timeZone ?: "UTC",
            )

            effectiveDb.withDoorTransactionAsync {
                effectiveDb.clazzEnrolmentDao().updateClazzEnrolmentInviteUid(
                    clazzEnrolmentInviteUid = clazzInvite.ciUid,
                    clazzEnrolmentUid = enrolmentUid,
                    updateTime = systemTimeInMillis()
                )

                effectiveDb.clazzInviteDao().updateInviteStatus(
                    status = ClazzInvite.STATUS_ACCEPTED,
                    ciUid = clazzInvite.ciUid,
                    updateTime = systemTimeInMillis(),
                )
            }

            return ClazzRedeemResult(systemImpl.getString(MR.strings.invite_code_redeemed))
        }else{
            //Update the status of clazz invite that invite code is declined
            effectiveDb.clazzInviteDao().updateInviteStatus(
                status = ClazzInvite.STATUS_DECLINED,
                ciUid = clazzInvite.ciUid,
                updateTime = systemTimeInMillis(),
            )

            return ClazzRedeemResult(systemImpl.getString(MR.strings.invite_declined))
        }
    }
}
