package com.ustadmobile.lib.rest.domain.invite

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.invite.ParseInviteUseCase
import com.ustadmobile.core.domain.invite.SendClazzInvitesUseCase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import io.github.aakira.napier.Napier
import kotlinx.html.currentTimeMillis


/**
 * UseCase server-side implementation to send invite link to contacts link username via email, sms,
 * or internal messaging. Invite is stored in database.
 *
 * Contacts receive a unique link.
 */
class SendClazzInvitesUseCaseServerImpl(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val parseInviteUseCase: ParseInviteUseCase,
    private val db: UmAppDatabase,
    private val learningSpace: LearningSpace,
): SendClazzInvitesUseCase {

    override suspend operator fun invoke(
        request: SendClazzInvitesUseCase.SendClazzInvitesRequest
    ) {
        val invitesToSend = parseInviteUseCase(request.contacts.joinToString(separator = ","))

        try {
            val newInvites = mutableListOf<ClazzInvite>()
            val resendInvites = mutableListOf<ClazzInvite>()

            invitesToSend.forEach { contact ->
                val existingInvite = db.clazzInviteDao().findClazzInviteFromContact(contact.text)

                if (existingInvite != null) {
                    // Update expiration for existing invite
                    resendInvites.add(existingInvite.copy(inviteExpire = existingInvite.inviteExpire + (7 * 24 * 60 * 60 * 1000)))
                } else {
                    // Create new invite
                    newInvites.add(
                        ClazzInvite(
                            ciPersonUid = request.personUid,
                            ciRoleId = request.role,
                            ciClazzUid = request.clazzUid,
                            inviteType = contact.inviteType,
                            inviteToken = uuid4().toString(),
                            inviteContact = contact.text,
                            inviteExpire = currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                        )
                    )
                }
            }

            db.withDoorTransactionAsync {
                if (newInvites.isNotEmpty()) {
                    db.clazzInviteDao().insertAll(newInvites)
                }
                if (resendInvites.isNotEmpty()) {
                    db.clazzInviteDao().updateAll(resendInvites)
                }
            }

            val clazzName = db.clazzDao().findByUidAsync(request.clazzUid)?.clazzName ?: ""
            (newInvites + resendInvites).forEach { invite ->
                val inviteLink = UstadUrlComponents(
                    learningSpace.url, ClazzInviteRedeemViewModel.DEST_NAME,
                    "inviteCode=${invite.inviteToken}"
                ).fullUrl()
                val emailSubject = "Invitation to $clazzName"

                when (invite.inviteType) {

                    ClazzInvite.EMAIL -> {
                        sendEmailUseCase.invoke(
                            emailSubject,
                            invite.inviteContact,
                            inviteLink)
                    }

                    ClazzInvite.PHONE -> sendSmsUseCase.invoke(
                        clazzName,
                        invite.inviteContact,
                        inviteLink)

                    ClazzInvite.INTERNAL_MESSAGE -> sendMessageUseCase.invoke(
                        clazzName,
                        invite.inviteContact,
                        inviteLink,
                        request.personUid
                    )
                }
            }
        } catch (e: Exception) {
            Napier.e(e) { "SendClazzInvitesUseCase: ${e.message}" }
            throw e
        }
    }
}


