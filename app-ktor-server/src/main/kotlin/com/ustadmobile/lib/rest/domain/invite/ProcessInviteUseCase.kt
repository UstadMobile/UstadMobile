package com.ustadmobile.lib.rest.domain.invite

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.invite.CheckContactTypeUseCase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import com.ustadmobile.lib.rest.domain.invite.sms.SendSmsUseCase
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.rest.domain.invite.message.SendMessageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProcessInviteUseCase(
    private val sendEmailUseCase: SendEmailUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val checkContactTypeUseCase: CheckContactTypeUseCase,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
) {
    data class InviteResult(
        val inviteSent: String
    )

    operator fun invoke(
        contacts: List<String>,
        clazzUid: Long,
        role: Long,
        personUid: Long
    ): InviteResult {

        val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
        coroutineScope.launch {

            contacts.forEach { contact ->
                val token = uuid4().toString()
                val inviteLink = UstadUrlComponents(endpoint.url, ClazzInviteViewModel.DEST_NAME, "inviteCode=$token"
                ).fullUrl()
                val validContacts = checkContactTypeUseCase.invoke(contact = contact)

                if (validContacts != null) {
                    db.clazzInviteDao.replace(
                        ClazzInvite(
                            ciPersonUid = personUid,
                            ciRoleId = role,
                            ciUid = clazzUid,
                            inviteType = validContacts.inviteType,
                            inviteToken = token
                        )
                    )


                    when (validContacts.inviteType) {
                        1 -> {
                            sendEmailUseCase.invoke(validContacts.text, inviteLink)
                        }

                        2 -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                sendSmsUseCase.invoke(validContacts.text, inviteLink)
                            }
                        }

                        3 -> {
                            sendMessageUseCase.invoke(validContacts.text, inviteLink, personUid)
                        }

                    }
                }
            }
        }
        coroutineScope.cancel()
        return InviteResult("invitation sent")
    }

}


