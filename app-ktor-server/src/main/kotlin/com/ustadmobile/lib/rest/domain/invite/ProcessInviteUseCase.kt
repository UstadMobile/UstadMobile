package com.ustadmobile.lib.rest.domain.invite

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase

class ProcessInviteUseCase(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
) {
    data class InviteResult(
        val inviteSent: String
    )

    operator fun invoke(
        contacts: List<InviteViaContactChip>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ): List<String> {
        // just to test that which usecase is calling
        val result:MutableList<String> = mutableListOf()
        val token = uuid4().toString()
        val inviteLink = UstadUrlComponents(endpoint.url, ClazzInviteViewModel.DEST_NAME, token).fullUrl()
        contacts.forEach { contact ->
            db.clazzInviteDao.insert(
                ClazzInvite(
                    ciPersonUid = personUid,
                    ciRoleId = role,
                    ciUid = clazzUid,
                    inviteType = contact.inviteType,
                    inviteToken = token
                )
            )


         val useCase  = when (contact.inviteType) {
                1 -> {
                    "sendEmailUseCase"
                }

                2 -> {
                    "sendSmsUseCase"
                }

                3 -> {
                    "sendMessageUseCase"
                }

                else -> null
            }
            result.add(useCase.toString())
        }

        return result
    }
}