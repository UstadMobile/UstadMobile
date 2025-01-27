package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzInvite

class ClazzInviteRedeemUseCase(
    private val enrolIntoCourseUseCase: EnrolIntoCourseUseCase,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {
    suspend operator fun invoke(
        inviteCode: String,
        isAccepting: Boolean,
        personUid: Long
    ): ClazzRedeemResult {
        val effectiveDb = (repo ?: db)
        val clazzInviteWithTimeZone = effectiveDb.clazzInviteDao().findClazzInviteEntityForInviteToken(inviteCode)
                ?: return ClazzRedeemResult("Invite code is invalid")

        val clazz = clazzInviteWithTimeZone.clazzInvite?.ciUid?.let {

            effectiveDb.clazzEnrolmentDao().findClazzEnrolmentEntityForClazzEnrolmentInviteUid(it)
        }

        if (clazz != null) {
            return ClazzRedeemResult("Invite code is already redeemed")
        } else {
            clazzInviteWithTimeZone.clazzInvite?.let { clazzInvite ->

                if (isAccepting) {
                    if (clazzInvite.inviteExpire < systemTimeInMillis()){
                        return ClazzRedeemResult("Invite code is expired")
                    }else{
                        enrolIntoCourseUseCase.invoke(
                            enrolment = ClazzEnrolment(
                                clazzUid = clazzInvite.ciClazzUid,
                                personUid = personUid,
                                role = clazzInvite.ciRoleId.toInt()
                            ), timeZoneId = clazzInviteWithTimeZone.timeZone ?: "UTC"
                        )

                        //updating clazzEnrolment table by adding ciUid to clazzEnrolmentInviteUid
                        effectiveDb.clazzEnrolmentDao().updateClazzEnrolmentInviteUid(
                            clazzInvite.ciUid,
                            clazzInvite.ciClazzUid
                        )

                        //Update the status of clazz invite that invite code is accepted
                        effectiveDb.clazzInviteDao().updateInviteStatus(ClazzInvite.STATUS_ACCEPTED, clazzInvite.ciUid)
                        return ClazzRedeemResult("Invite code redeemed successfully")
                    }


                }else{

                    //Update the status of clazz invite that invite code is declined
                    effectiveDb.clazzInviteDao().updateInviteStatus(ClazzInvite.STATUS_DECLINED, clazzInvite.ciUid)
                    return ClazzRedeemResult("Invitation Declined")

                }
            } ?: return ClazzRedeemResult("Invite code is invalid")

        }

    }
}

data class ClazzRedeemResult(
    val message: String
)
