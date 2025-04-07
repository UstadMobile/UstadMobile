package com.ustadmobile.lib.rest.domain.invite

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable


/**
 * UseCase to resend invite link to contact
 */

class ResendInviteUseCase(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val db: UmAppDatabase,
    private val learningSpace: LearningSpace,
    private val repo: UmAppDatabase?,
) {
    @Serializable
    data class InviteResult(
        val inviteSent: String
    )

    suspend operator fun invoke(
        contact: String,
        personUid: Long
    ): InviteResult {
        try {
            val effectiveDb = repo ?: db


            val clazzInvite = effectiveDb.clazzInviteDao().findClazzInviteFromContact(contact)
            val clazzName = effectiveDb.clazzDao().findByUidAsync(clazzInvite.ciClazzUid)?.clazzName ?: ""

            val inviteLink =
                UstadUrlComponents(
                    learningSpace.url, ClazzInviteRedeemViewModel.DEST_NAME,
                    "inviteCode=${clazzInvite.inviteToken}"
                ).fullUrl()
            val emailSubject = "Invitation to $clazzName"
            when (clazzInvite.inviteType) {
                1 -> {
                    clazzInvite.inviteContact.let { sendEmailUseCase.invoke(emailSubject, it, inviteLink) }
                }

                2 -> {
                    clazzInvite.inviteContact.let { sendSmsUseCase.invoke(clazzName, it, inviteLink) }
                }

                3 -> {
                    clazzInvite.inviteContact.let { sendMessageUseCase.invoke(clazzName, it, inviteLink, personUid) }
                }
            }


        } catch (e: Exception) {
            Napier.e(e) { "ResendInviteUseCase: ${e.message}" }
            InviteResult("invitation error: ${e.message}")
        }
        return InviteResult("invitation sent")

    }
}
